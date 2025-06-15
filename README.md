MediLabo Solutions - Diabetes Risk Assessment

Microservices application for diabetes risk assessment built with Spring Boot and Vue.js.

---

## 🧰 Technology Stack

- **Backend**: Java 21, Spring Boot 3.4.1, Spring Security, Spring Cloud Gateway, Eureka
- **Frontend**: Vue.js 3, Pinia (Store Manager), Axios
- **Databases**: MySQL 8, MongoDB
- **Infrastructure**: Docker, Docker-Compose, ⚠️ *Kubernetes (on the roadmap)*
- **Communication**: RestAPI, OpenFeign, ⚠️ *Kafka or RabbitMQ (on the roadmap)*
- **Observability**: ⚠️ *ELK Stack, OpenTelemetry / Prometheus / Grafana (on the roadmap)*

---

## 🏗️ Architecture

- **Service Discovery**: Eureka Server
- **API Gateway**: Spring Cloud Gateway - (routing, auth)
- **Frontend**: Vue.js
- **Patient**: business microservice - MySQL
- **Notes**: business microservice - MongoDB
- **Assessments**: business microservice - (aggregation, logic, results)

---

## 🔧 In Progress

### Security Enhancements
- HTTP-only cookies implementation (`branch: httponly`)
- Keycloak integration (`branch: keycloak`)

### Resilience
- Resilience4J - circuit breakers and retry patterns

---

## ✅ Already done

### Infrastructure
- Eureka Server for service discovery ✅
- API Gateway with routing ✅
- Docker Compose setup ✅
- Health checks for all services ✅
- Feign client for inter-service communication ✅

### Security
- Services protected behind the gateway ✅
- Spring Security integration : JWT authentication (Token propagation in the headers) ✅

### Data Layer
- Patients microservice with MySQL ✅
- Notes microservice with MongoDB ✅

### Frontend
- Vue.js application ✅
- Pinia state management (Authentication store) ✅
- Local storage integration ✅

### Features
- View/update/add patient information ✅
- View/add notes ✅
- Diabetes risk assessments ✅
- (Notes && Age && gender) based risk rules ✅

### Monitoring
- Correlation ID on the requests/responses headers (filter in the Gateway) ✅
- Logging the tracing across microservices (checking the headers) ✅


