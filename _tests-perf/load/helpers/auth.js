import http from 'k6/http';
import { BACK_URL } from '../../config.js';

export function getAuthToken() {
    const username = __ENV.USERNAME;
    const password = __ENV.PASSWORD;

    if (!username || !password) {
        throw new Error('USERNAME and PASSWORD environment variables must be set');
    }

    const credentials = JSON.stringify({
        username: username,
        password: password,
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
