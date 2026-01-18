# 🚀 Orderly Deployment Guide

This guide covers deploying the Orderly microservices application.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Load Balancer                            │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   Frontend    │    │  Auth Service │    │ Order Service │
│   (React)     │    │   :8085       │    │    :8081      │
│    :3000      │    └───────────────┘    └───────────────┘
└───────────────┘             
        │            ┌───────────────┐    ┌───────────────┐
        │            │  Inventory    │    │ Notification  │
        │            │   Service     │    │   Service     │
        └──────────▶ │    :8082      │    │    :8083      │
                     └───────────────┘    └───────────────┘
                              
                     ┌───────────────┐
                     │Recommendation │
                     │   Service     │
                     │    :8084      │
                     └───────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   MongoDB     │    │    Redis      │    │    Kafka      │
│    :27017     │    │    :6379      │    │    :9092      │
└───────────────┘    └───────────────┘    └───────────────┘
```

---

## Option 1: Local Development

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven 3.8+
- Node.js 18+

### Quick Start

```bash
# 1. Start infrastructure (MongoDB, Redis, Kafka, Elasticsearch)
cd orderly
docker-compose up -d

# 2. Wait for services to be healthy (~30 seconds)
docker-compose ps

# 3. Start backend services (in separate terminals)
cd auth-service && mvn spring-boot:run &
cd order-service && mvn spring-boot:run &
cd inventory-service && mvn spring-boot:run &
cd notification-service && mvn spring-boot:run &
cd recommendation-service && mvn spring-boot:run &

# 4. Start frontend
cd frontend && npm install && npm run dev
```

### Access Points
- **Frontend**: http://localhost:3000
- **Auth Service**: http://localhost:8085
- **Order Service**: http://localhost:8081
- **Inventory Service**: http://localhost:8082
- **Notification Service**: http://localhost:8083
- **Recommendation Service**: http://localhost:8084

---

## Option 2: Docker Deployment (Recommended)

### Build Docker Images

```bash
# Build all services
docker-compose -f docker-compose.prod.yml build

# Or build individually
docker build -t orderly/auth-service ./auth-service
docker build -t orderly/order-service ./order-service
docker build -t orderly/inventory-service ./inventory-service
docker build -t orderly/notification-service ./notification-service
docker build -t orderly/recommendation-service ./recommendation-service
docker build -t orderly/frontend ./frontend
```

### Run Everything

```bash
# Start all services
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Stop all
docker-compose -f docker-compose.prod.yml down
```

---

## Option 3: Cloud Deployment

### 🆓 Oracle Cloud Free Tier (Recommended for Free Hosting)

**Complete step-by-step guide**: See [ORACLE_CLOUD_DEPLOYMENT.md](./ORACLE_CLOUD_DEPLOYMENT.md)

**Quick Summary:**
- **Always Free**: 4 ARM VMs, 24GB RAM, 200GB storage
- **No time limits** - Services run 24/7
- **Perfect for production** - Full control, no sleeping

**Quick Start:**
```bash
# 1. Create Oracle Cloud account (cloud.oracle.com)
# 2. Create ARM VM (4 OCPU, 24GB RAM)
# 3. SSH in and run:
git clone https://github.com/KuldeepSingh3011/orderly.git
cd orderly
sudo apt update && sudo apt install -y docker.io docker-compose
sudo usermod -aG docker $USER
docker compose -f docker-compose.prod.yml up -d --build
```

**Your app will be live at:** `http://YOUR_PUBLIC_IP`

---

### AWS (ECS/EKS)

1. **Push images to ECR**:
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com

# Tag and push
docker tag orderly/auth-service:latest <account>.dkr.ecr.us-east-1.amazonaws.com/orderly-auth:latest
docker push <account>.dkr.ecr.us-east-1.amazonaws.com/orderly-auth:latest
# ... repeat for other services
```

2. **Use AWS services**:
   - **ECS Fargate** for containers
   - **DocumentDB** for MongoDB
   - **ElastiCache** for Redis
   - **MSK** for Kafka
   - **OpenSearch** for Elasticsearch
   - **Application Load Balancer** for routing
   - **CloudFront** for frontend CDN

### Google Cloud (GKE)

1. **Push to GCR**:
```bash
docker tag orderly/auth-service gcr.io/PROJECT_ID/orderly-auth
docker push gcr.io/PROJECT_ID/orderly-auth
```

2. **Use GCP services**:
   - **GKE** for Kubernetes
   - **MongoDB Atlas** or **Firestore**
   - **Memorystore** for Redis
   - **Cloud Pub/Sub** or Confluent Kafka

### Azure (AKS)

1. **Push to ACR**:
```bash
az acr login --name orderlyregistry
docker tag orderly/auth-service orderlyregistry.azurecr.io/orderly-auth
docker push orderlyregistry.azurecr.io/orderly-auth
```

2. **Use Azure services**:
   - **AKS** for Kubernetes
   - **Cosmos DB** (MongoDB API)
   - **Azure Cache for Redis**
   - **Azure Event Hubs** for Kafka

---

## Environment Variables

### Backend Services

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATA_MONGODB_URI` | MongoDB connection | `mongodb://localhost:27017/orderly` |
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka servers | `localhost:9092` |
| `JWT_SECRET` | JWT signing secret | (set in production!) |
| `JWT_EXPIRATION_MS` | Token expiry | `3600000` (1 hour) |

### Frontend

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_URL` | Backend API URL | `http://localhost:8081` |

---

## Health Checks

```bash
# Check all services
curl http://localhost:8081/actuator/health  # Order
curl http://localhost:8082/actuator/health  # Inventory
curl http://localhost:8083/actuator/health  # Notification
curl http://localhost:8084/actuator/health  # Recommendation
curl http://localhost:8085/actuator/health  # Auth
```

---

## Production Checklist

- [ ] Set strong `JWT_SECRET` (at least 256 bits)
- [ ] Enable HTTPS/TLS on all endpoints
- [ ] Configure proper CORS for frontend domain
- [ ] Set up database authentication (MongoDB, Redis)
- [ ] Configure Kafka security (SASL/SSL)
- [ ] Set up monitoring (Prometheus, Grafana)
- [ ] Configure log aggregation (ELK, CloudWatch)
- [ ] Set resource limits (CPU, memory)
- [ ] Enable health checks and auto-restart
- [ ] Configure backups for MongoDB/Redis
- [ ] Set up CI/CD pipeline

---

## Scaling

### Horizontal Scaling
- Order Service: 2-5 replicas (handles most traffic)
- Inventory Service: 2-3 replicas
- Auth Service: 2-3 replicas
- Notification Service: 1-2 replicas
- Recommendation Service: 1-2 replicas

### Database Scaling
- MongoDB: Replica Set (3 nodes minimum)
- Redis: Cluster mode or Sentinel
- Kafka: 3+ brokers with replication factor 3

---

## Troubleshooting

### Service won't start
```bash
# Check logs
docker logs orderly-auth-service

# Check if ports are in use
lsof -i :8085
```

### MongoDB connection issues
```bash
# Test connection
mongosh mongodb://localhost:27017/orderly
```

### Kafka issues
```bash
# List topics
docker exec orderly-kafka kafka-topics --list --bootstrap-server localhost:9092
```

---

## Support

For issues or questions, check the logs:
```bash
docker-compose logs -f <service-name>
```
