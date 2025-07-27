import http from 'k6/http';
import { config } from '../config.js';

export function getAuthToken() {
    if (!config.username || !config.password) {
        throw new Error('USERNAME and PASSWORD environment variables must be set');
    }

    const credentials = JSON.stringify({
        username: config.username,
        password: config.password,
    });

    const params = {
        headers: config.httpConfig.headers,
        timeout: config.httpConfig.timeout,
    };

    const res = http.post(`${config.backUrl}/login`, credentials, params);

    if (res.status !== 200) {
        throw new Error(`login error: ${res.status} ${res.body}`);
    }

    return res.json().accessToken;
}
