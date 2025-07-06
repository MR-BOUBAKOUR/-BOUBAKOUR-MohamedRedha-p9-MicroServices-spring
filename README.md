MediLabo Solutions - Diabetes Risk Assessment

Microservices application for diabetes risk assessment built with Spring Boot and Vue.js.

---

### 🏗️ Architecture

- **Frontend**: Vue.js
- **API Gateway**: Spring Cloud Gateway - PostgreSQL *(routing, auth)*
- **Service discovery**: Eureka Server
- **Patients**: business microservice - MySQL
- **Notes**: business microservice - MongoDB
- **Assessments**: business microservice - *(aggregation, logic, results)*
- **E2E Tests**: end-to-end test module simulating full doctor journey to validate system-wide behavior

---

### 🧰 Technology stack (If on the roadmap → ⚠️)

- **Backend**: Java 21, Spring Boot 3.4.1, Spring Security, Spring Cloud Gateway, Eureka
- **Frontend**: Vue.js 3, Pinia (Store Manager), Axios
- **Databases**: MySQL 8, MongoDB, PostgreSQL
- **Communication**: RestAPI, OpenFeign, ⚠️ *Kafka or RabbitMQ*
- **Infrastructure**: Docker, Docker-Compose, ⚠️ *Kubernetes*
- **Testing**: JUnit 5, TestContainers, RestAssured, Awaitility
- **Observability**: ⚠️ *ELK Stack or OpenTelemetry/Prometheus/Grafana*
- **Resiliency**: ⚠️ *Resilience4J*

---

### 🧪 Testing Strategy

#### ✅ Unit & Integration Tests

- Implemented for: **Patients**, **Notes**, **Assessments** and the **Gateway**
- Covers core business logic, database operations, and Feign communication

#### ✅ End-to-End (E2E) Tests

The full journey test simulates a real doctor's workflow using `DoctorJourneyE2ETest`:
- Covers a 4-step risk evolution: *None → Borderline → In Danger → Early onset*
- Validates data flow across all services
- Verifies patient creation, note insertion and risk assessment logic
- Uses **Awaitility** to ensure service readiness and propagation
- Executed in a real environment with **Docker Compose**

---

### 🔧 Security implementation versions

| Branch | Description | Status |
|--------|-------------|--------|
| `jwt-header` | JWT Access Token in Authorization header only | ✅ |
| `access-header-refresh-httponly` | Access token in header + Refresh token in HttpOnly cookie | ✅ |
| `all-httponly` | Full HttpOnly for Access & Refresh tokens + CSRF token | ❌ *Abandoned*<br/>Too complex for minimal security gain. Modern SPA setups with SameSite and CORS provide sufficient protection. |
| `oauth2-access&refresh` | OAuth2 with Google + classic login (Access & Refresh tokens for both) | ✅ *Current* |
| `keycloak` | Keycloak integration | 🕒 *Postponed*<br/>Will be reconsidered after progress on event-driven design and observability. |

---

### ❌ Out of scope

- **Spring Cloud Config Server**: No centralized configuration management. *(used in a different project, with RabbitMQ as the refresh trigger and a GitHub repository for versioning and storing configurations)*
- **Secrets Manager**: Secrets are managed via environment variables.