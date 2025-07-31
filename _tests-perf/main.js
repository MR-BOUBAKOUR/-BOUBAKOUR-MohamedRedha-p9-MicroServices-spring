import { config } from './config.js';
import { getAuthToken } from './helpers/auth.js';
import * as realisticTraffic from './tests/realistic-traffic.js';
import * as loginProcess from './tests/login-process.js';
import * as patientRecord from './tests/patient-record.js';

const TEST_TYPE = __ENV.TEST_TYPE || 'realistic';
const TEST_PROFILE = __ENV.TEST_PROFILE || 'load';

export let options = {
    stages: config.profiles[TEST_PROFILE].stages,
    thresholds: config.profiles[TEST_PROFILE].thresholds,
};

// K6 lifecycle: setup() runs ONCE at test start and returns shared data.
export function setup() {
    console.log(`Starting [${TEST_TYPE}] test with [${TEST_PROFILE}] profile`);
    console.log(`Target URL: ${config.backUrl}`);

    const token = getAuthToken();
    return { token, testType: TEST_TYPE };
}

// default() runs for EACH Virtual User on EVERY iteration, receiving setup's return value as 'data' parameter.
export default function (data) {
    switch (data.testType) {
        case 'realistic':
            realisticTraffic.default(data);
            break;
        case 'login' :
            loginProcess.default(data);
            break;
        case 'patient':
            patientRecord.default(data);
            break;
        default:
            console.warn(`Unknown test type: ${data.testType}, falling back to realistic`);
            realisticTraffic.default(data);
    }
}