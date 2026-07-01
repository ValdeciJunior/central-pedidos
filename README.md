# Central de Pedidos

Uma API de alta performance desenvolvida em **Java 21** com **Spring Boot 3.5.16** para recepção massiva, cálculo e 
processamento assíncrono de pedidos. O sistema foi desenhado seguindo os princípios da **Arquitetura Hexagonal (Ports and Adapters)**, 
garantindo isolamento total do domínio, resiliência e idempotência sob estresse de concorrência.

---

## Arquitetura e Decisões Técnicas

### Arquitetura Hexagonal (Ports & Adapters)
O núcleo da aplicação (Domínio e Regras de Negócio) é 100% puro e isolado de frameworks. 
* **Use Cases:** Camada que expõe as capacidades do sistema de forma segregada (ex: `ProcessOrderUseCase`, `FindOrderUseCase`),
  respeitando o princípio **ISP (Interface Segregation Principle)** do SOLID.
* **Ports & Adapters:** A comunicação com o banco de dados (PostgreSQL) e clientes HTTP externos é feita estritamente através de
  interfaces (*Ports*), cujas implementações de infraestrutura (*Adapters*) são injetadas pelo Spring.

### Processamento Assíncrono (@Async)
Para suportar picos agressivos de requisições sem causar indisponibilidade, o endpoint de entrada adota uma estratégia não-bloqueante. 
O controller valida o payload e delega o processamento de cálculos e persistência para um pool de threads em segundo plano (`orderExecutor`). 
O cliente recebe uma resposta imediata **HTTP 202 Accepted**, liberando a conexão de I/O.

### Concorrência Real
* **Unique Constraints:** Proteção no nível do motor do banco de dados relacional para o campo `external_order_id`, impedindo
  que requisições duplicadas enviadas simultaneamente gerem duplicatas na base de dados.
* **Lock Otimista (`@Version`):** Uso do controle de concorrência otimista do JPA para proteger atualizações concorrentes nos registros
  de pedidos já existentes, evitando o cenário de *Lost Update*.

### Circuit Breaker
A integração com sistemas externos é protegida por mecanismos de **Circuit Breaker** integrados com filas de contingência. 
Falhas na rede ou timeouts não travam a thread de execução e os dados falhos são isolados de forma resiliente para auditoria.

---

## Tecnologias Utilizadas

* **Java 21** e Gradle (Groovy)
* **Spring Boot 3.5.16** (JPA, Web, Async)
* **PostgreSQL 16** (Alpine Edition via Docker)
* **MapStruct** (Transformações de DTO para Entity em tempo de compilação)
* **HikariCP** (Pool de conexões)
* **Testcontainers** (Ambiente de banco isolado para testes de integração)
* **k6** (Ferramenta de testes de carga e estresse)

---

## Como Executar o Ambiente Local

### Pré-requisitos
* Docker e Docker Compose instalados.
* Java 21 configurado localmente.

### 1. Subir o Banco de Dados no Docker
O ambiente está isolado na porta externa **5434** para evitar conflitos com instâncias nativas do PostgreSQL na máquina:
```bash
docker compose up -d order-db
```

### 2. Execute o projeto.
Esecute o projeto utilizando a sua IDE ou use o comando:
```bash
./gradlew bootRun
```

## Endpoints
Segue abaixo os curls das requisições do projeto com seus respectivos contratos

### Receber/Processar Pedido (POST)
```bash
curl --location 'http://localhost:8080/api/v1/orders' \
--header 'Content-Type: application/json' \
--data '{
    "externalOrderId": "PEDIDO-2026-XYZ-001",
    "customerName": "Valdeci Rolim Junior",
    "items": [
        {
            "productId": "PROD-NOTE-001",
            "quantity": 1,
            "unitPrice": 4500.00
        },
        {
            "productId": "PROD-MOUSE-002",
            "quantity": 2,
            "unitPrice": 150.00
        }
    ]
}'
```

### Consultar Pedido (GET)
```bash
curl --location 'http://localhost:8080/api/v1/orders/PEDIDO-2026-XYZ-001'
```

## Teste de integração
Para executar os teste de integração, basta executar os teste da classe `OrderIntegrationTest` ou executar o comando:
```bash
./gradlew test
```

## Teste de stress com o K6
Basta ir na raiz do projeto e executar o comando:
```bash
k6 run k6-stress-test.js
```
