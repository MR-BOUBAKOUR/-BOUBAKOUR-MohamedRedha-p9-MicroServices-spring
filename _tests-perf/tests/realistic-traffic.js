import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { config } from '../config.js';
import { getAuthToken } from '../helpers/auth.js';
import { Counter } from 'k6/metrics';

export const loginCount = new Counter('login');
export const homeCount = new Counter('home');
export const patientRecordCount = new Counter('patientRecord');
export const createPatientCount = new Counter('createPatient');
export const createNoteCount = new Counter('createNote');
export const createCriticalNoteCount = new Counter('createCriticalNote');
export const assessmentCount = new Counter('assessment');

let token;
let tokenIssuedAt;
const TOKEN_REFRESH_THRESHOLD_MS = 14 * 60 * 1000; // 14 minutes

// Ensure the refresh of the token to avoid expiration during long-running tests (expire after 15 minutes)
function ensureFreshToken(initialToken) {

    const tokenAge = Date.now() - tokenIssuedAt;

    if (!token || tokenAge > TOKEN_REFRESH_THRESHOLD_MS) {
        token = getAuthToken();
        tokenIssuedAt = Date.now();
    }
    return token || initialToken;
}

function triggerAssessment(patientId, headers) {
    assessmentCount.add(1);
    const response = http.get(`${config.backUrl}/v1/assessments/${patientId}`, { headers });
    check(response, { 'GET assessment 200': (r) => r.status === 200 });
    return response;
}

export default function (data) {

    const authToken = ensureFreshToken(data.token);

    const headers = {
        Authorization: `Bearer ${authToken}`,
        ...config.httpConfig.headers,
    };

    const patientId = Math.floor(Math.random() * 50) + 1;

    const scenario = Math.random();

    // 15%
    if (scenario < 0.15) {
        loginCount.add(1);
        group('Login', () => {
            const token = getAuthToken();
            check(token, { 'login successful': (t) => !!t });
        });
    // 20%
    } else if (scenario < 0.35) {
        homeCount.add(1);
        group('Home - Patient list', () => {
            const response = http.get(`${config.backUrl}/v1/patients`, { headers });
            check(response, { 'GET patients 200': (r) => r.status === 200 });
        });
    // 30%
    } else if (scenario < 0.65) {
        patientRecordCount.add(1);
        group('Patient record', () => {
            const responses = http.batch([
                ['GET', `${config.backUrl}/v1/patients/${patientId}`, null, { headers }],
                ['GET', `${config.backUrl}/v1/notes/${patientId}`, null, { headers }],
            ]);
            check(responses[0], { 'GET patients 200': (r) => r.status === 200 });
            check(responses[1], { 'GET notes 200': (r) => r.status === 200 });

            if (responses[0].status === 200 && responses[1].status === 200) {
                triggerAssessment(patientId, headers);
            }
        });
    // 10%
    } else if (scenario < 0.75) {
        createPatientCount.add(1);
        group('Create patient', () => {
            const payloadPatient = JSON.stringify({
                firstName: 'Test',
                lastName: `User-${__VU}-${Date.now()}`,
                birthDate: '1985-10-15',
                gender: 'M',
                address: '',
                phone: '',
            });
            const response = http.post(`${config.backUrl}/v1/patients`, payloadPatient, { headers });
            check(response, { 'POST patient 201': (r) => r.status === 201 });
        });
    // 20%
    } else if (scenario < 0.95) {
        createNoteCount.add(1);
        group('Create simple note', () => {
            const payloadNote = JSON.stringify({
                patId: patientId,
                note: 'Le patient se sent fatigué ces derniers temps, sans signes de risque.',
            });
            const response = http.post(`${config.backUrl}/v1/notes`, payloadNote, { headers });
            check(response, { 'POST note 201': (r) => r.status === 201 });

            if (response.status === 201) {
                triggerAssessment(patientId, headers);
            }
        });
    // 5%
    } else {
        createCriticalNoteCount.add(1);
        group('Create critical note (level 4 assessment → notification)', () => {
            const payloadNote = JSON.stringify({
                patId: patientId,
                note: 'Patient présente une Hémoglobine A1C élevée, Microalbumine positive, Taille normale, Poids stable, Fumeur actif, taux de Cholestérol anormal, épisodes de Vertiges fréquents, récente Rechute, et Réaction allergique suspectée.\n',
            });
            const response = http.post(`${config.backUrl}/v1/notes`, payloadNote, { headers });
            check(response, { 'POST note (critical) 201': (r) => r.status === 201 });

            if (response.status === 201) {
                triggerAssessment(patientId, headers);
            }
        });
    }

    sleep(Math.random() * 2 + 1);
}