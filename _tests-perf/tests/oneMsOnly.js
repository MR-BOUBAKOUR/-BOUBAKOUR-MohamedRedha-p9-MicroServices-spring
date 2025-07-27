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

    // const headers = {
    //     Authorization: `Bearer 1`,
    //     'medilabo-solutions-correlation-id': 1,
    //     ...config.httpConfig.headers,
    // };

    const validIds = [1, 2, 3, 4];
    const patientId = validIds[Math.floor(Math.random() * validIds.length)];

    const res = http.get(`${config.backUrl}/v1/patients/${patientId}`, { headers });
    // const res = http.get(`http://assessments:8080/assessments/${patientId}`, { headers });
    // const res = http.get(`http://patients:8100/patients/${patientId}`, { headers });

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(Math.random() * 2 + 1);
    // sleep(1);
}
