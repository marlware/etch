# Etch
Etch is a distributed notification hub using Apache Kafka to decouple high-volume business orders from notification delivery pipelines.

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

- Multi-service architecture implemented
- Spring Cloud Gateway routing requests
- Orders published to Kafka
- Notification service consuming Kafka events
- Email and SMS services processing notifications
- Retry strategy implemented
- Dead Letter Queue working
- Redis used for deduplication/idempotency
- MySQL persistence complete
- Structured logging with correlation IDs
- Dockerized local development environment
- AWS ECS deployment working
- GitHub Actions CI/CD pipeline complete
- Integration tests passing
- API documentation with Swagger/OpenAPI