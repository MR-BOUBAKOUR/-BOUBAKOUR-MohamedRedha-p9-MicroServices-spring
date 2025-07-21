import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuration
export let options = {
    vus: 10,             // Number of "virtual users" in parallel
    duration: '30s',     // Total duration of the test
};

// Each "virtual user" will execute this function
export default function () {

    const request = http.get('https://localhost:8071/login');

    // Validate that the response status is 200 OK
    check(request, {
        'status is 200': (response) => response.status === 200,
    });

    // Wait 1 second before the next iteration (simulates think time)
    sleep(1);
}