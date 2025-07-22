import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from '../config.js';

export let options = {
    scenarios: {
        normal_users: {
            executor: 'constant-vus',
            vus: 10,
            duration: '30s',
        },
        spike: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 20 },
                { duration: '10s', target: 20 },
                { duration: '10s', target: 0 },
            ],
        },
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/login`);
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(Math.random() * 2 + 1);
}