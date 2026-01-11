# Orderly

An event-driven order processing system built with microservices architecture.

## Overview

Orderly demonstrates real-world backend patterns including event sourcing, CQRS, and distributed systems concepts. Built for learning and showcasing production-grade microservices development.

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Order Service  │     │Inventory Service│     │  Notification   │
│                 │     │                 │     │    Service      │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │      Apache Kafka       │
                    │    (Event Streaming)    │
                    └─────────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌────────▼────────┐     ┌────────▼────────┐     ┌────────▼────────┐
│    MongoDB      │     │      Redis      │     │ Recommendation  │
│   (Database)    │     │    (Cache)      │     │    Service      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Boot 3.2 | Application framework |
| Apache Kafka | Event streaming |
| MongoDB | Primary database |
| Redis | Caching & session storage |
| Docker | Containerization |

## Services

| Service | Port | Description |
|---------|------|-------------|
| order-service | 8081 | Order management, cart operations |
| inventory-service | 8082 | Product catalog, stock management |
| notification-service | 8083 | Email/SMS notifications |
| recommendation-service | 8084 | AI-powered recommendations |

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

## Quick Start

```bash
# Start infrastructure
docker-compose up -d

# Build all modules
mvn clean install

# Run order service
cd order-service && mvn spring-boot:run
```

## Project Structure

```
orderly/
├── common-lib/          # Shared events, DTOs, constants
├── order-service/       # Order & cart management
├── inventory-service/   # Product & stock management
├── notification-service/# Notification handling
├── recommendation-service/ # AI recommendations
├── docker-compose.yml   # Infrastructure setup
└── pom.xml             # Parent POM
```

## License

MIT
