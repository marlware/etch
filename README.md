# Etch
Etch is a distributed notification hub using Apache Kafka to decouple high-volume business orders from notification delivery pipelines.

---

## Running locally

Requires Docker. From the repo root:

```bash
docker compose up -d --build
```

This starts Kafka (KRaft, single node), MySQL, Redis, Prometheus, Grafana,
and all five services. First build takes a few minutes (each service's
Dockerfile builds the Maven reactor from scratch); subsequent starts are
fast.

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Order Service | http://localhost:8081 |
| Notification Service | http://localhost:8082 |
| Email Service (mock) | http://localhost:8083 |
| SMS Service (mock) | http://localhost:8084 |
| Grafana | http://localhost:3000 (anonymous viewer access, or admin/admin) |
| Prometheus | http://localhost:9090 |

Each service also serves Swagger UI at `/swagger-ui.html` and its OpenAPI
document at `/v3/api-docs`.

### Try it end to end

Get a token, then create an order through the gateway (bypassing it and
hitting order-service directly on 8081 also works, and skips the auth step):

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo"}' | jq -r .accessToken)

curl -s -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"orderNumber":"ORD-1001","total":49.99,"channels":["EMAIL"]}' | jq
```

Demo users 1-3 (seeded by Flyway) are `ada.lovelace@example.com`,
`grace.hopper@example.com`, and `alan.turing@example.com`. A few seconds
later:

```bash
AUTH=(-H "Authorization: Bearer $TOKEN")   # everything but /auth and /actuator needs this

curl -s "${AUTH[@]}" http://localhost:8080/notifications/order/1 | jq       # delivery status
curl -s "${AUTH[@]}" "http://localhost:8080/notifications/1/history" | jq  # full audit trail
curl -s "${AUTH[@]}" "http://localhost:8080/admin/dlt" | jq                # anything dead-lettered
```

Email/SMS services fail a configurable fraction of requests on purpose
(`EMAIL_FAILURE_RATE`, `SMS_FAILURE_RATE` in docker-compose.yml, default
10%), so retries and occasional dead-lettering are visible without any
extra setup.

### Running the test suite

```bash
mvn test           # unit tests only
mvn verify          # unit + Testcontainers integration tests (needs Docker)
```

## Project layout

```
services/           order-service, notification-service, email-service,
                     sms-service, api-gateway -- one Spring Boot app each
shared/              common-events, common-dto, common-security, common-utils
infrastructure/      docker-compose support files, ECS task definitions
.github/workflows/   CI/CD pipeline
```

---

# Etch - Distributed Notification Hub

## Project Setup

- Create a multi-module Spring Boot project
- Configure Java 21
- Create separate microservices:
  - API Gateway
  - Order Service
  - Notification Service
  - Email Service
  - SMS Service (mock implementation)
- Configure MySQL
- Configure Redis
- Configure Apache Kafka
- Configure Docker
- Configure GitHub Actions
- Prepare AWS deployment configuration

---

## Microservice Architecture

### API Gateway
Responsibilities:
- Route requests
- Authentication
- Rate limiting
- Load balancing
- Forward requests to downstream services

### Order Service
Responsibilities:
- Receive business orders
- Validate requests
- Persist orders
- Publish Kafka events

### Notification Service
Responsibilities:
- Consume Kafka events
- Determine notification channels
- Dispatch notification jobs
- Retry failed deliveries
- Publish failed events to Dead Letter Queue

### Email Service
Responsibilities:
- Simulate email sending
- Log delivery status
- Return success/failure

### SMS Service
Responsibilities:
- Simulate SMS sending
- Log delivery status
- Return success/failure

---

## Database Design

### MySQL Tables

Users
- id
- email
- phone
- created_at

Orders
- id
- user_id
- order_number
- status
- total
- created_at

Notifications
- id
- order_id
- channel
- status
- retry_count
- created_at

NotificationAudit
- id
- notification_id
- event
- timestamp

---

## Kafka Architecture

Topics:
- order-created
- notification-requested
- notification-sent
- notification-failed

Dead Letter Queue:
- notification-dlt

Flow:

Client

↓

API Gateway

↓

Order Service

↓

Kafka (order-created)

↓

Notification Service

↓

Email/SMS Service

↓

Kafka (notification-sent)

or

↓

notification-dlt

---

## Order API

Implement:

POST /orders
GET /orders
GET /orders/{id}

Features:
- Create order
- Store in MySQL
- Publish Kafka event
- Return immediately without waiting for notifications

---

## Notification Pipeline

When an order is created:

1. Receive Kafka event
2. Validate payload
3. Determine delivery channels
4. Send notification
5. Save delivery status
6. Publish completion event

Notifications should be fully asynchronous.

---

## Kafka Producer

Create producers for:
- OrderCreatedEvent
- NotificationRequestedEvent
- NotificationSentEvent
- NotificationFailedEvent

Use JSON serialization.

---

## Kafka Consumers

Implement consumers for:
- OrderCreatedEvent
- NotificationRequestedEvent

Requirements:
- Manual acknowledgment
- Error handling
- Logging
- Retry support
- Idempotent processing

---

## Retry Strategy

Implement configurable retries.

Example:
- Retry 1
- Retry 2
- Retry 3
- Dead Letter Queue

Retry failures:
- Network timeout
- Email service unavailable
- SMS service unavailable

Do not retry:
- Invalid payload
- Missing user
- Validation failures

---

## Dead Letter Queue

Create notification-dlt topic.

Failed messages should include:
- Original payload
- Failure reason
- Timestamp
- Retry count

Provide endpoint:

GET /admin/dlt

to inspect failed events.

---

## Redis

Use Redis for:

- Notification deduplication
- Idempotency keys
- Rate limiting
- Short-lived caching

Example:
Prevent duplicate notifications from being sent twice for the same order.

---

## Spring Cloud Gateway

Configure routes:

/orders/**
→ Order Service

/notifications/**
→ Notification Service

Features:
- Routing
- Load balancing
- Authentication filter
- Request logging
- Rate limiting

---

## Validation

Validate:
- Required fields
- Valid email
- Valid phone
- Positive order totals
- Duplicate order numbers
- Supported notification channels

---

## Exception Handling

Create global exception handlers.

Handle:
- ValidationException
- KafkaException
- NotificationException
- ResourceNotFoundException
- DuplicateOrderException

Return consistent JSON responses.

---

## Logging

Use structured logging.

Log:
- Incoming requests
- Kafka publishes
- Kafka consumes
- Notification attempts
- Retry attempts
- DLQ events
- Errors

Include correlation IDs across services.

---

## Docker

Create Dockerfiles for every service.

docker-compose should include:

- API Gateway
- Order Service
- Notification Service
- Email Service
- SMS Service
- Kafka
- Zookeeper (or KRaft Kafka)
- MySQL
- Redis

Support local startup with one command.

---

## AWS Deployment

Deploy to AWS ECS.

Infrastructure:
- ECS Fargate
- Amazon RDS (MySQL)
- ElastiCache (Redis)
- Amazon ECR
- CloudWatch Logs

Environment variables:
- Database
- Kafka
- Redis
- JWT (optional if authentication is added)

---

## GitHub Actions

Create CI/CD pipeline.

On push:

- Build all services
- Run unit tests
- Run integration tests
- Build Docker images
- Push images to Amazon ECR
- Deploy to ECS

---

## Testing

Unit tests:

- Kafka producers
- Kafka consumers
- Order service
- Notification service
- Retry logic
- Validation
- Redis caching

Integration tests:

- Kafka with Testcontainers
- MySQL with Testcontainers
- Redis with Testcontainers
- End-to-end notification flow

---

## Monitoring

Add Spring Boot Actuator.

Expose:
- Health
- Metrics
- Readiness
- Liveness

Track:
- Notifications sent
- Notifications failed
- Retry count
- DLQ size
- Kafka consumer lag (if available)

---

## Nice-to-Have Features

- Email templates
- SMS templates
- Push notifications
- Webhook notifications
- Notification preferences
- Scheduled notifications
- Notification history
- Admin dashboard
- Prometheus metrics
- Grafana dashboards

---

## Suggested Project Structure

services/

- api-gateway/
- order-service/
- notification-service/
- email-service/
- sms-service/

shared/

- common-events/
- common-dto/
- common-security/
- common-utils/

infrastructure/

- docker/
- kubernetes/ (optional)
- github-actions/

---

## Definition of Done

- [x] Multi-service architecture implemented
- [x] Spring Cloud Gateway routing requests
- [x] Orders published to Kafka
- [x] Notification service consuming Kafka events
- [x] Email and SMS services processing notifications
- [x] Retry strategy implemented
- [x] Dead Letter Queue working
- [x] Redis used for deduplication/idempotency
- [x] MySQL persistence complete
- [x] Structured logging with correlation IDs
- [x] Dockerized local development environment
- [x] AWS ECS deployment configuration written (task definitions +
      pipeline); actually deploying requires an AWS account and the repo
      secrets described in `infrastructure/aws/README.md`, neither of
      which exist for this sample project
- [x] GitHub Actions CI/CD pipeline complete
- [x] Integration tests passing (`mvn verify`; needs a Docker daemon
      reachable the standard way -- see note below for Docker Desktop on
      Windows)
- [x] API documentation with Swagger/OpenAPI

> **Testcontainers on Windows + Docker Desktop:** if `mvn verify` can't
> find a Docker environment even though `docker info` works fine from the
> shell, Docker Desktop's named pipe may not be the one Testcontainers
> defaults to. Set `DOCKER_HOST` to whatever `docker context inspect`
> shows for the active context (e.g.
> `npipe:////./pipe/dockerDesktopLinuxEngine`) before running Maven. This
> doesn't come up in CI, which runs on Linux with a standard Docker
> socket.