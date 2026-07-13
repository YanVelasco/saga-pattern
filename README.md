# Arquitetura de Microsservicos - Padrao Saga Orquestrado

Projeto de referencia para **Saga Orquestrada** com microsservicos usando:

- Java 17 + Spring Boot
- Kafka (Redpanda)
- MongoDB (order-service)
- PostgreSQL (product-validation, payment, inventory)
- Docker Compose para ambiente completo

O fluxo e iniciado via REST no `order-service`, publicado no topico `start-saga` e controlado pelo `orchestrator-service` ate finalizar com sucesso ou falha (rollback).

---

## 1. Visao geral da arquitetura

Servicos da solucao:

| Servico | Porta | Responsabilidade | Banco |
|---|---:|---|---|
| order-service | 3000 | Recebe pedido REST, cria order/evento e inicia saga | MongoDB |
| orchestrator-service | 8080 | Decide proximo passo da saga (roteamento por source/status) | - |
| product-validation-service | 8090 | Valida existencia dos produtos | PostgreSQL |
| payment-service | 8091 | Calcula valor, registra pagamento e rollback (refund) | PostgreSQL |
| inventory-service | 8092 | Debita/retorna estoque e controla rollback | PostgreSQL |
| Redpanda Console | 8081 | Inspecao de topicos e mensagens Kafka | - |

Infra no `docker-compose.yml`:

- `kafka` (Redpanda)
- `redpanda-console`
- `order_db` (MongoDB)
- `product_db`, `payment_db`, `inventory_db` (PostgreSQL)

---

## 2. Topicos Kafka

Topicos usados no fluxo:

- `start-saga`
- `orchestrator`
- `product-validation-success`
- `product-validation-fail`
- `payment-success`
- `payment-fail`
- `inventory-success`
- `inventory-fail`
- `finish-success`
- `finish-fail`
- `notify-ending`

---

## 3. Regras de roteamento da saga (orquestrador)

O orquestrador usa `source + status` para decidir o proximo topico:

| Source | Status | Proximo topico |
|---|---|---|
| ORCHESTRATOR | SUCCESS | product-validation-success |
| ORCHESTRATOR | FAIL | finish-fail |
| PRODUCT_VALIDATION_SERVICE | SUCCESS | payment-success |
| PRODUCT_VALIDATION_SERVICE | ROLLBACK_PENDING | product-validation-fail |
| PRODUCT_VALIDATION_SERVICE | FAIL | finish-fail |
| PAYMENT_SERVICE | SUCCESS | inventory-success |
| PAYMENT_SERVICE | ROLLBACK_PENDING | payment-fail |
| PAYMENT_SERVICE | FAIL | product-validation-fail |
| INVENTORY_SERVICE | SUCCESS | finish-success |
| INVENTORY_SERVICE | ROLLBACK_PENDING | inventory-fail |
| INVENTORY_SERVICE | FAIL | payment-fail |

---

## 4. Fluxo de negocio

### 4.1 Fluxo de sucesso

1. `POST /api/orders` no `order-service`
2. `order-service` salva pedido/evento e publica em `start-saga`
3. `orchestrator-service` envia para `product-validation-success`
4. `product-validation-service` valida produtos e devolve para `orchestrator`
5. `orchestrator-service` envia para `payment-success`
6. `payment-service` processa pagamento e devolve para `orchestrator`
7. `orchestrator-service` envia para `inventory-success`
8. `inventory-service` baixa estoque e devolve para `orchestrator`
9. `orchestrator-service` envia para `finish-success`
10. `orchestrator-service` publica em `notify-ending`
11. `order-service` consome `notify-ending` e persiste evento final

### 4.2 Fluxo de falha/compensacao

Quando um servico falha, ele publica `ROLLBACK_PENDING` e o orquestrador dispara compensacoes:

- Falha no `product-validation-service` -> pode encerrar em `finish-fail`
- Falha no `payment-service` -> rollback em `payment-fail`, depois pode acionar rollback de validacao
- Falha no `inventory-service` -> rollback em `inventory-fail`, depois rollback de pagamento/validacao conforme transicoes

Cada servico registra historico em `eventHistory` com `source`, `status`, `message` e `createdAt`.

---

## 5. Contrato REST (entrada da saga)

Endpoint principal:

- `POST http://localhost:3000/api/orders`

Payload REST:

```json
{
  "products": [
    {
      "product": {
        "code": "string",
        "unitValue": 0
      },
      "quantity": 0
    }
  ]
}
```

Exemplo de sucesso:

```json
{
  "products": [
    {
      "product": {
        "code": "LAPTOP_DELL_XPS15",
        "unitValue": 5999.99
      },
      "quantity": 1
    },
    {
      "product": {
        "code": "MOUSE_LOGITECH_MX3",
        "unitValue": 299.99
      },
      "quantity": 2
    }
  ]
}
```

Exemplo de falha (produto invalido):

```json
{
  "products": [
    {
      "product": {
        "code": "PRODUTO_INVALIDO_XYZ",
        "unitValue": 999.99
      },
      "quantity": 1
    }
  ]
}
```

---

## 6. Consultas e observabilidade

### 6.1 Consultar eventos no order-service

Endpoint:

- `GET http://localhost:3000/api/event`
- `GET http://localhost:3000/api/event?orderId=<orderId>`
- `GET http://localhost:3000/api/event?transactionId=<transactionId>`

### 6.2 Console Kafka (Redpanda)

- URL: `http://localhost:8081`
- Permite acompanhar mensagens por topico e validar o encadeamento da saga.

---

## 7. Como executar com Docker Compose

Na raiz do repositorio:

```bash
docker compose up -d --build
```

Para acompanhar logs:

```bash
docker compose logs -f orchestrator-service
docker compose logs -f order-service
docker compose logs -f product-validation-service
docker compose logs -f payment-service
docker compose logs -f inventory-service
```

Parar ambiente:

```bash
docker compose down
```

Remover volumes (reset completo de dados):

```bash
docker compose down -v
```

---

## 8. Como executar localmente (sem Docker dos apps)

Opcao comum: subir apenas bancos + kafka com Docker e rodar apps via Gradle.

Em cada servico:

```bash
./gradlew bootRun
```

No Windows:

```powershell
.\gradlew.bat bootRun
```

---

## 9. Massa de dados inicial

Os servicos `product-validation-service` e `inventory-service` usam `import.sql` para popular:

- lista de produtos validos (codes)
- estoque inicial por produto

Arquivos:

- `product-validation-service/src/main/resources/import.sql`
- `inventory-service/src/main/resources/import.sql`

---

## 10. Cenarios de teste incluidos no repositorio

- `test-payloads.txt`: payloads e cenarios de sucesso/falha para testes por topico (Kafka) e casos de uso do orquestrador.
- `orchestrator-test-scenarios.txt`: guia de testes do fluxo de orquestracao.

Para teste funcional completo, execute:

1. `POST /api/orders` com payload de sucesso.
2. Acompanhe topicos no Redpanda Console.
3. Consulte `GET /api/event` filtrando por `orderId`/`transactionId`.
4. Repita com payload de falha para observar compensacao.

---

## 11. Estrutura de pastas

```text
.
├── docker-compose.yml
├── order-service
├── orchestrator-service
├── product-validation-service
├── payment-service
├── inventory-service
├── test-payloads.txt
└── orchestrator-test-scenarios.txt
```

---

## 12. Notas importantes

- O servico no compose esta padronizado como `orchestrator-service` (mesmo nome do modulo).
- O calculo de pagamento aplica arredondamento para 2 casas decimais.
- A rastreabilidade da saga fica centralizada no `eventHistory` e no endpoint `/api/event`.
