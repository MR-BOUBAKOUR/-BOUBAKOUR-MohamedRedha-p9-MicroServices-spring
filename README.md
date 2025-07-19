``MediLabo Solutions - Diabetes Risk Assessment

Microservices application for diabetes risk assessment built with Spring Boot and Vue.js.

📄 [Documentation & Reports](https://mr-boubakour.github.io/-BOUBAKOUR-MohamedRedha-p9-MicroServices-spring/) — includes **Javadoc** and **JaCoCo reports** for the microservices where documentation and test coverage bring the most value.

---

### 🏗️ Architecture

- **Frontend**: Vue.js
- **API Gateway**: Spring Cloud Gateway - PostgreSQL *(routing, auth)*
- **Service discovery**: Eureka Server
- **Patients**: business microservice - MySQL
- **Notes**: business microservice - MongoDB
- **Assessments**: business microservice - *(aggregation, logic, results)*
- **Notifications**: business microservice - *(high-risk alerts via email)*
- **E2E Tests**: end-to-end test module simulating full doctor journey to validate system-wide behavior

---

### 🧰 Technology stack  (⚠️ → on the roadmap)

| Category                             | Technologies / Tools                                                                                                                                                                                  |
|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Backend**                          | Java 21, Spring Boot 3.4.1, Spring Security, Eureka, Spring Cloud Gateway *(Reactive)*                                                                                                                |
| **Frontend**                        | Vue.js 3, Pinia, Axios                                                                                                                                                                                |
| **Data Storage**                     | MySQL, MongoDB, PostgreSQL *(Reactive)*                                                                                                                                                               |
| **Inter-service Communication**      | REST API, OpenFeign, RabbitMQ                                                                                                                                                                         |
| **Testing & Automation**             | unit        : Mockito, MockMvc<br>integration : TestContainers, MockMvc<br>e2e         : TestContainers, RestAssured, Awaitility<br>⚠️ load       : K6 (in progress)                                  |
| **Containerisation & Orchestration** | Docker, Docker-Compose, ⚠️ *Kubernetes*                                                                                                                                                               |
| **CI/CD**                            | GitHub Actions, GitHub Pages (JaCoCo & JavaDoc), Docker Hub                                                                                                                                           |
| **Observability & Monitoring**       | logs   : Alloy, Loki, Grafana<br>metrics : Micrometer, Prometheus, Grafana<br>traces  : OpenTelemetry, Tempo, Grafana<br><br>custom dashboard : based on JVM (Micrometer) & Spring Boot obs templates |
| **Resilience & Fault Tolerance**     | ⚠️ *Resilience4J*                                                                                                                                                                                     |
| **AI Integration**                  | ⚠️ *Ollama (Llama 3.2 3B)* - Local LLM for basic diabetes risk assessment                                                                                                                             |



---

### 🔒 Security implementation versions

| Branch | Description | Status                                                                                                                           |
|--------|-------------|----------------------------------------------------------------------------------------------------------------------------------|
| `jwt-header` | JWT Access Token in Authorization header only | ✅                                                                                                                                |
| `access-header-refresh-httponly` | Access token in header + Refresh token in HttpOnly cookie | ✅                                                                                                                                |
| `all-httponly` | Full HttpOnly for Access & Refresh tokens + CSRF token | ❌ *Abandoned*<br/>Too complex for minimal security gain. Modern SPA setups with SameSite and CORS provide sufficient protection. |
| `oauth2-access&refresh` | OAuth2 with Google + classic login (Access & Refresh tokens for both) | ✅ *Current*                                                                                                                      |
| `keycloak` | Keycloak integration | 🕒 *Postponed*<br/>Will be reconsidered after progress on the observability & monitoring part.                                   |

---

### 📊 Observability & Monitoring

The system includes comprehensive observability to ensure reliability and simplify production diagnostics.

- **Logs**: collected via Alloy, centralized and indexed in Loki
- **Metrics**: exposed by Micrometer, scraped by Prometheus
- **Distributed Traces**: captured by OpenTelemetry, stored in Tempo

All data is visualized and analyzed through Grafana.

The customized dashboard is based on the two popular dashboards : JVM (Micrometer) & Spring Boot Observability.

![Custom Observability Dashboard](img/dashboard-image.png)

---

### 🔔 Event-Driven

The system implements asynchronous communication using **RabbitMQ** for critical notifications:

- **High-Risk Assessment Events**: when a patient is assessed as `"Early onset"`, the **Assessments** service publishes an event to the `high-risk-assessments` queue
- **No duplicates**: the alert is triggered only when the risk changes to `"Early onset"`
- **Email Notifications**: the **Notifications** service consumes these events and sends automated email alerts to healthcare providers (emails are intercepted using **Mailtrap** during development)

---

### 🧪 Testing Strategy

#### ✅ Unit & Integration Tests

- Implemented for: **Patients**, **Notes**, **Assessments** and the **Gateway**
- Covers core business logic, database operations, and Feign communication

#### ✅ End-to-End (E2E) Tests

The full journey test simulates a real doctor's workflow using `DoctorJourneyE2ETest`:
- Verifies patient creation, note insertion and risk assessment logic
- Covers a 4-step risk evolution: *None → Borderline → In Danger → Early onset*
- Confirms high-risk email delivery by checking the notifications service logs
- Validates data flow across all services
- Uses **Awaitility** to ensure service readiness and propagation
- Executed in a real environment with **Docker Compose**

---

### 🚀 CI/CD Pipelines

- `push_dev_ci.yml`: runs unit tests on modified microservices when pushing to `dev`
- `pr_main_ci-cd.yml`: builds, tests, generates JaCoCo & JavaDocs, deploys docs to GitHub Pages (on PR to `main`)
- `merge_main_cd.yml`: pushes Docker images to Docker Hub after PR is merged into `main`

---

### ❌ Out of scope

- **Spring Cloud Config Server**: no centralized configuration management. *(used in a different project, with RabbitMQ as the refresh trigger and a GitHub repository for versioning and storing configurations)*
- **Secrets Manager**: secrets are managed via environment variables.
- **Front-end testing** : deprioritized to focus efforts on back-end reliability and service integration.