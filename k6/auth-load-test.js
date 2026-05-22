import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,        // 10 virtual users
    duration: '30s', // selama 30 detik
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    // Test login
    const loginPayload = JSON.stringify({
        identifier: 'admin',
        password: 'Admin123!',
    });

    const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(loginRes, {
        'login status 200': (r) => r.status === 200,
        'login has token': (r) => JSON.parse(r.body).token !== undefined,
    });

    sleep(1);
}