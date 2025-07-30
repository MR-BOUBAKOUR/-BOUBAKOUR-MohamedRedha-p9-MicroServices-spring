import http from 'k6/http';
import { check, sleep } from 'k6';
import { config } from '../config.js';
import { getAuthToken } from '../helpers/auth.js';

export default function () {
    // Perform POST /login on each VU iteration to simulate full login process
    const token = getAuthToken();

    const headers = {
        Authorization: `Bearer ${token}`,
        ...config.httpConfig.headers,
    };

    const res = http.get(`${config.backUrl}/v1/patients`, { headers });

    check(res, { 'patients 200': (r) => r.status === 200 });

    sleep(Math.random() * 2 + 1);
}