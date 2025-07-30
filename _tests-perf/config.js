export const config = {

    backUrl: __ENV.BACK_URL,

    // Used only on the local dev process
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
                { duration: '1m', target: 1 },
                { duration: '2m', target: 3 },
                { duration: '1m', target: 0 }
            ],
            thresholds: {
                http_req_duration: ['p(95)<5000'],
                http_req_failed: ['rate<0.1']
            }
        },

        load: {
            stages: [
                { duration: '1m', target: 40 },
                { duration: '3m', target: 100 },
                { duration: '1m', target: 160 },
                { duration: '3m', target: 160 },
                { duration: '2m', target: 60 },
                { duration: '1m', target: 0 }
            ],
            thresholds: {
                http_req_duration: ['p(95)<2000'],
                http_req_failed: ['rate<0.05']
            }
        },

        stress: {
            stages: [
                { duration: '1m', target: 50 },
                { duration: '2m', target: 150 },
                { duration: '1m', target: 250 },
                { duration: '2m', target: 300 },
                { duration: '1m', target: 350 },
                { duration: '2m', target: 400 },
                { duration: '2m', target: 0 }
            ],
            thresholds: {
                http_req_duration: ['p(95)<8000'],
                http_req_failed: ['rate<0.20']
            }
        },

        spike: {
            stages: [
                { duration: '2m', target: 30 },
                { duration: '30s', target: 400 },
                { duration: '1m', target: 200 },
                { duration: '2m', target: 30 },
                { duration: '1m', target: 0 }
            ],
            thresholds: {
                http_req_duration: ['p(95)<10000'],
                http_req_failed: ['rate<0.30']
            }
        },

        soak: {
            stages: [
                { duration: '2m', target: 40 },
                { duration: '60m', target: 40 },
                { duration: '2m', target: 0 }
            ],
            thresholds: {
                http_req_duration: ['p(95)<3000'],
                http_req_failed: ['rate<0.05']
            }
        },
    }
}