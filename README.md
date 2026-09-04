# Order Processing System

A full-stack order processing application built as a single repository with separate service and UI folders.

## Tech Stack

- Backend: Java 21, Spring Boot, Spring Data JPA, Flyway, PostgreSQL
- Frontend: React, TypeScript, Vite, Axios, React Router
- Testing: JUnit 5, Spring Boot Test, Mockito, Testcontainers
- Operations: Docker, Docker Compose, Swagger UI, Spring Actuator

## Repository Structure

```text
order-processing-system/
  order-process-service/
  order-process-ui/
  docker-compose.yml
  README.md
  OrderProcessing-design.md
  OrderProcessing-prompt.md
```

## Core Features

- Create an order with multiple items
- Retrieve order details by order ID
- List all orders with optional status filtering
- Update order status with allowed transitions
- Cancel an order only when it is `PENDING`
- Scheduled job that moves `PENDING` orders to `PROCESSING` every 5 minutes

## Architecture Summary

The backend follows a layered modular design:

- `controller` handles HTTP requests and response codes
- `service` owns business rules and order workflows
- `repository` handles persistence with JPA
- `validation` centralizes status transition policy
- `scheduler` runs the automated pending-to-processing job

The frontend is a lightweight React app that consumes the backend REST APIs and exposes:

- create order form
- order list with status filter
- order detail page
- cancel action for pending orders

## High-Level Architecture

### Logical Layers

1. `API Layer`
   Handles HTTP requests, validation, and response mapping.
2. `Application/Service Layer`
   Orchestrates use cases and enforces business rules.
3. `Domain Layer`
   Contains core entities, enums, and status transition logic.
4. `Persistence Layer`
   Manages repositories and database interaction.
5. `Scheduler/Background Layer`
   Runs the 5-minute job to update stale pending orders.

### Architecture Diagram

```text
Client / UI / Postman
        |
        v
 REST Controllers
        |
        v
 Application Services
        |
        v
 Domain Model + Business Rules
        |
        v
 Repositories / JPA
        |
        v
   PostgreSQL

Scheduled Job ---> Order Service ---> Repository ---> PostgreSQL
```

## Local Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- npm
- PostgreSQL 16+ running locally on `localhost:5432`

## PostgreSQL Setup

Create a local database and user that match the backend defaults:

```sql
CREATE DATABASE order_processing;
```

Use the credentials expected by [`order-process-service/src/main/resources/application.yml`](c:/Users/dell/Documents/Projects/order-processing-system/order-process-service/src/main/resources/application.yml):

- username: `postgres`
- password: `postgres`

You can change those values in `application.yml` if your local database uses different credentials.

## Run Backend Locally

```bash
cd order-process-service
mvn spring-boot:run
```

Backend URLs:

- API base: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator health: `http://localhost:8080/actuator/health`

## Run Frontend Locally

```bash
cd order-process-ui
npm install
npm run dev
```

Frontend URL:

- `http://localhost:5173`

The frontend defaults to calling `http://localhost:8080/api`. Override it with `VITE_API_BASE_URL` if needed.

## Run with Docker Compose

```bash
docker compose up --build
```

Container URLs:

- UI: `http://localhost:4173`
- service: `http://localhost:8080`
- postgres: `localhost:5432`

The backend uses the `docker` Spring profile in Compose so it connects to the `postgres` container automatically.

## API Endpoints

- `POST /api/orders`
- `GET /api/orders/{orderId}`
- `GET /api/orders?status=PENDING`
- `PATCH /api/orders/{orderId}/status`
- `POST /api/orders/{orderId}/cancel`

## Sample cURL Commands

Create an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-1001",
    "items": [
      {
        "productId": "P-101",
        "productName": "Keyboard",
        "quantity": 2,
        "unitPrice": 45.50
      },
      {
        "productId": "P-202",
        "productName": "Mouse",
        "quantity": 1,
        "unitPrice": 15.00
      }
    ]
  }'
```

Get an order by ID:

```bash
curl http://localhost:8080/api/orders/1
```

List orders by status:

```bash
curl http://localhost:8080/api/orders?status=PENDING
```

Update an order status:

```bash
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PROCESSING"
  }'
```

Cancel an order:

```bash
curl -X POST http://localhost:8080/api/orders/1/cancel
```

## Testing

Run backend tests:

```bash
cd order-process-service
mvn test
```

The integration tests use Testcontainers with PostgreSQL, so Docker must be available when running the test suite.

## Notes

- Database schema is managed through Flyway migration `V1__create_orders_tables.sql`
- If the standalone scripts in `database/` created the schema first, Flyway baselines it at version `1` on the next service startup
- The scheduler interval can be changed through `app.scheduling.pending-to-processing-rate-ms`
- CORS is enabled for `http://localhost:5173` by default
