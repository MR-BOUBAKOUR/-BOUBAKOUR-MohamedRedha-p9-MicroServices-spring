import http from 'k6/http';
import { check, sleep } from 'k6';
import { config } from '../config.js';
import { getAuthToken } from '../helpers/auth.js';

export let options = {
    stages: config.profiles.load.stages,
    thresholds: config.profiles.load.thresholds,
};

export function setup() {
    const token = getAuthToken();
    return { token };
}

export default function (data) {
    const headers = {
        Authorization: `Bearer ${data.token}`,
        ...config.httpConfig.headers,
    };

    const validIds = [1, 2, 3, 4];
    const patientId = validIds[Math.floor(Math.random() * validIds.length)];

    const responses = http.batch([
        ['GET', `${config.backUrl}/v1/patients/${patientId}`, null, { headers }],
        ['GET', `${config.backUrl}/v1/notes/${patientId}`, null, { headers }],
        ['GET', `${config.backUrl}/v1/assessments/${patientId}`, null, { headers }],
    ]);

    check(responses[0], { 'GET patients 200': (r) => r.status === 200 });
    check(responses[1], { 'GET notes 200': (r) => r.status === 200 });
    check(responses[2], { 'GET assessments 200': (r) => r.status === 200 });

    sleep(Math.random() * 2 + 1);
}
