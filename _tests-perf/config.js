
export const config = {
    // URLs
    frontUrl: __ENV.FRONT_URL,
    backUrl: __ENV.BACK_URL,

    // Used only on the local dev process
    // frontUrl: 'https://[::1]:5173',
    // backUrl: 'https://[::1]:8071',

    // Credentials for the auth
    username: __ENV.USERNAME,
    password: __ENV.PASSWORD,

    httpConfig: {
        timeout: '30s',
        headers: {
            'User-Agent': 'MediLabo-K6-Test/1.0',
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    },

    profiles: {

        smoke: {
            stages: [
                { duration: '30s', target: 1 }
            ],
            thresholds: {
                http_req_duration: ['p(95)<3000'],
                http_req_failed: ['rate<0.1']
            }
        },

        load: {
            stages: [
                { duration: '1m', target: 5 },
                { duration: '3m', target: 5 },
                { duration: '1m', target: 0 }
            ],
            thresholds: {
                http_req_duration: ['p(95)<2000'],
                http_req_failed: ['rate<0.05']
            }
        },

        stress: {
            stages: [
                { duration: '2m', target: 10 },
                { duration: '3m', target: 20 },
                { duration: '2m', target: 0 }
            ],
            thresholds: {
                http_req_duration: ['p(95)<5000'],
                http_req_failed: ['rate<0.15']
            }
        }
    },

}
