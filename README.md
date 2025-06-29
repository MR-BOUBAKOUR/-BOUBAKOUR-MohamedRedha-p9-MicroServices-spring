MediLabo Solutions - Diabetes Risk Assessment

Microservices application for diabetes risk assessment built with Spring Boot and Vue.js.

---

### 🏗️ Architecture

- **Service discovery**: Eureka Server
- **API Gateway**: Spring Cloud Gateway - *(routing, auth)*
- **Frontend**: Vue.js
- **Patient**: business microservice - MySQL
- **Notes**: business microservice - MongoDB
- **Assessments**: business microservice - *(aggregation, logic, results)*

---

### 🧰 Technology stack

- **Backend**: Java 21, Spring Boot 3.4.1, Spring Security, Spring Cloud Gateway, Eureka
- **Frontend**: Vue.js 3, Pinia (Store Manager), Axios
- **Databases**: MySQL 8, MongoDB
- **Communication**: RestAPI, OpenFeign, ⚠️ *Kafka or RabbitMQ (on the roadmap)*
- **Observability**: ⚠️ *ELK Stack, OpenTelemetry / Prometheus / Grafana (on the roadmap)*
- **Infrastructure**: Docker, Docker-Compose, ⚠️ *Kubernetes (on the roadmap)*

---

### ❌ Out of scope

- **Spring Cloud Config Server**: No centralized configuration management. *(used in a different project, with RabbitMQ as the refresh trigger and a GitHub repository for versioning and storing configurations)*
- **Secrets Manager**: Secrets are managed via environment variables.

---

### 🔧 In progress

#### Security implementation versions
- JWT header only (`branch: jwt-header`) ✅


- Access token in header + Refresh token in HttpOnly cookie (`branch: access-header-refresh-httponly`) ✅ current


- Access & refresh tokens full HttpOnly implementation + CSRF token (`branch: all-httponly`) ❌

    *Abandoned: adds unnecessary complexity without significant security benefits. Modern SPA architecture with SameSite cookies and proper CORS configuration provides equivalent protection.*


- OAuth2 login via Spring Security (`branch: oauth2`) 🔧


- Keycloak integration (`branch: keycloak`)

#### Resilience
- Resilience4J - circuit breakers and retry patterns

---

### ✅ Already done

#### Infrastructure
- Eureka Server for service discovery
- API Gateway with routing
- Docker Compose setup
- Health checks for all services
- Feign client for inter-service communication

#### Security
- Services protected behind the gateway
- Spring Security integration depending on the branch

#### Data layer
- Patients microservice with MySQL
- Notes microservice with MongoDB

#### Frontend
- Vue.js application
- Pinia state management (Authentication store)
- Token management depending on the branch

#### Features
- View/update/add patient information
- View/add notes
- Diabetes risk assessments
- (Notes && Age && gender) based risk rules

#### Monitoring
- Correlation ID on request/response headers (filter in the Gateway)
- Distributed tracing logs across the microservices