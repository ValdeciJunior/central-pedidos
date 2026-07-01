import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    scenarios: {
        default: {
            executor: 'constant-vus',
            vus: 50,
            duration: '40s',
            gracefulStop: '30s',
        },
    },
    thresholds: {
        // Garantir que a taxa de erro seja baixa e o tempo de resposta aceitável
        http_req_failed: ['rate<0.05'], // Erros menores que 5%
        http_req_duration: ['p(95)<200'], // 95% das requisições abaixo de 200ms
    },
};

export default function () {
    const url = 'http://localhost:8080/api/v1/orders';

    // Gerando um ID aleatório para passar pela validação de Unique Constraint do banco
    const randomOrderId = `K6-PEDIDO-${randomString(8)}`;

    const payload = JSON.stringify({
        externalOrderId: randomOrderId,
        customerName: "Teste de Carga k6",
        items: [
            {
                productId: "PROD-K6-001",
                quantity: 2,
                unitPrice: 150.00
            }
        ]
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    // Como o endpoint é assíncrono, o status esperado de sucesso é 202 (Accepted)
    check(res, {
        'status deve ser 202': (r) => r.status === 202,
    });

    // Pequena pausa entre as requisições para simular comportamento real
    sleep(0.1);
}