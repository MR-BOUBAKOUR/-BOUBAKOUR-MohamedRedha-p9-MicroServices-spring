import http from 'k6/http';
import { BACK_URL, USERNAME, PASSWORD } from '../config.js';

export function getAuthToken() {

    if (!USERNAME || !PASSWORD) {
        throw new Error('USERNAME and PASSWORD environment variables must be set');
    }

    const credentials = JSON.stringify({
        username: USERNAME,
        password: PASSWORD,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(`${BACK_URL}/login`, credentials, params);

    if (res.status !== 200) {
        throw new Error(`login error: ${res.status} ${res.body}`);
    }

    const body = JSON.parse(res.body);
    return body.accessToken;
}
