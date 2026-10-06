# Etch on AWS ECS

Deployment target for the definition of done's "AWS ECS deployment working."
This documents the topology the task definitions in `ecs/` assume and how
the CI/CD pipeline (`.github/workflows/ci-cd.yml`) drives it. It is
infrastructure-as-documentation, not infrastructure-as-code -- provisioning
the resources below (via Terraform/CloudFormation/console) is a one-time
setup step outside this repo's scope.

## Topology

- **ECS Cluster** (Fargate): `etch-cluster`, one service per app + one for
  the Kafka broker.
- **Service discovery**: an ECS Service Connect / Cloud Map namespace named
  `etch.internal`, so services reach each other exactly the way they do in
  docker-compose (`order-service.etch.internal:8081`, etc.) instead of via
  hardcoded IPs.
- **Amazon RDS (MySQL)**: one instance, two schemas (`etch_orders`,
  `etch_notifications`) -- mirrors the local docker-compose setup, provisioned
  via the init script equivalent run once against RDS.
- **Amazon ElastiCache (Redis)**: one cluster, used by notification-service
  for idempotency.
- **Kafka**: for this project's scope, a single-node broker running as its
  own ECS Fargate service (same `apache/kafka` image as docker-compose) with
  an EFS-backed volume for `/var/lib/kafka`. A production deployment would
  replace this with Amazon MSK; that swap only touches
  `KAFKA_BOOTSTRAP_SERVERS`, nothing in the application code.
- **Amazon ECR**: one repository per service (`etch-order-service`,
  `etch-notification-service`, `etch-email-service`, `etch-sms-service`,
  `etch-api-gateway`).
- **CloudWatch Logs**: one log group per service under `/ecs/etch/*`
  (`awslogs-create-group` is set so the first deploy creates it).
- **Application Load Balancer**: fronts `api-gateway` only; the other four
  services are only reachable inside the cluster's private subnets.

## Secrets and configuration

Task definitions pull non-secret config from plain `environment` entries and
everything else from SSM Parameter Store / Secrets Manager via `secrets`,
consistent with how docker-compose passes the same values through plain
env vars locally:

| Value | Source |
|---|---|
| `DB_HOST` | SSM `/etch/shared/db-host` |
| `DB_USERNAME` / `DB_PASSWORD` | SSM param / Secrets Manager, per service |
| `REDIS_HOST` | SSM `/etch/shared/redis-host` |

## IAM

- `etchEcsTaskExecutionRole`: pulls images from ECR, writes to CloudWatch
  Logs, reads the SSM parameters/secrets referenced above (standard ECS
  task execution role plus an inline policy scoped to the `etch/*` paths).
- One task role per service (`etchOrderServiceTaskRole`, etc.), currently
  empty/minimal since none of the services call other AWS APIs at runtime --
  kept separate rather than shared so future per-service AWS access (e.g. SES
  for real email sending) doesn't require broadening every service's
  permissions.

## One-time service creation

Task definitions are registered and updated on every deploy by CI, but the
ECS *services* that reference them are created once:

```bash
aws ecs create-service \
  --cluster etch-cluster \
  --service-name order-service \
  --task-definition etch-order-service \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_IDS],securityGroups=[$APP_SG_ID],assignPublicIp=DISABLED}" \
  --service-connect-configuration "enabled=true,namespace=etch.internal"
```

Repeat per service, swapping the `--load-balancer` flag in for
`api-gateway` to attach it to the ALB target group instead of relying on
Service Connect alone.

## How CI deploys

1. Build and push each service's image to its ECR repository, tagged with
   the commit SHA.
2. Render each `ecs/*-task-def.json` template, substituting
   `${AWS_ACCOUNT_ID}` / `${AWS_REGION}` and the new image tag.
3. Register the rendered task definition and update the corresponding ECS
   service to use it (`aws-actions/amazon-ecs-deploy-task-definition`),
   which triggers a rolling deployment.

See `.github/workflows/ci-cd.yml` for the exact steps.
