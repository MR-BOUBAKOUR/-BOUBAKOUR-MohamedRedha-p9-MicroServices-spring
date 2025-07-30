``MediLabo Solutions - Diabetes risk assessment

Microservices application for diabetes risk assessment built with Spring Boot and Vue.js.

🔍 [Performance analysis](_doc/performance-analysis.md) — the main performance bottleneck was the system resource contention caused by running the app, monitoring, and load tests on the same machine — not the application itself.

📄 [Documentation & Reports](https://mr-boubakour.github.io/-BOUBAKOUR-MohamedRedha-p9-MicroServices-spring/) — includes **Javadoc** and **JaCoCo reports** for the microservices where documentation and test coverage bring the most value.

---

### 🏗️ Architecture

![big_picture.png](_img/big_picture.png)

<details>
<summary>Architecture details (click to expand)</summary>

- **Microservices architecture**, each service owning a clear business responsibility.
- **Single page application** built with Vue.js 3, communicating securely with the API gateway.
- **Reactive API gateway** centralizes routing, authentication, and authorization.
- **Service discovery** via Eureka enables dynamic routing and scalability.
- **Synchronous** REST for standard service communication; **asynchronous** messaging via RabbitMQ for critical events.
- **Core business services**:
  - Patients service — manages patient records with relational storage.
  - Notes service — handles medical notes using a NoSQL store.
  - Assessments service — evaluates diabetes risk, detects risk level changes, and emits high-risk events.
  - Notifications service — consumes events and sends alert emails to healthcare professionals.
- **Integrated observability**: logs, metrics, and traces collected and visualized via a custom Grafana dashboard.
- **Multi-layered testing strategy**:
  - Unit and integration tests on core services.
  - End-to-end tests cover full doctor journey across services.
  - Performance testing to evaluate system behavior under load
- **CI/CD** automates testing, documentation, and image publishing.

</details>



---

### 🧰 Technology stack

| Category                             | Technologies / Tools (⚠️ on the roadmap - 🕒 Postponed)                                                                                                                                              |
|--------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Backend**                          | Java 21, Spring Boot 3.4.1, Spring Security, Eureka, Spring Cloud Gateway *(Reactive)*                                                                                                               |
| **Frontend**                        | Vue.js 3, Pinia, Axios                                                                                                                                                                               |
| **Data Storage**                     | MySQL, MongoDB, PostgreSQL *(Reactive)*                                                                                                                                                              |
| **Inter-service Communication**      | REST API, OpenFeign, RabbitMQ                                                                                                                                                                        |
| **Testing & Automation**             | unit        : Mockito, MockMvc<br>integration : TestContainers, MockMvc<br>e2e         : TestContainers, RestAssured, Awaitility<br> performance       : K6                                          |
| **Containerisation & Orchestration** | Docker, Docker-Compose, 🕒 *Kubernetes*                                                                                                                                                              |
| **CI/CD**                            | GitHub Actions, GitHub Pages (JaCoCo & JavaDoc), Docker Hub                                                                                                                                          |
| **Observability & Monitoring**       | logs   : Alloy, Loki, Grafana<br>metrics : Micrometer, Prometheus, Grafana<br>traces  : OpenTelemetry, Tempo, Grafana<br><br>custom dashboard : based on JVM (Micrometer) & Spring Boot obs templates |
| **Resilience & Fault Tolerance**     | ⚠️ *Resilience4J*                                                                                                                                                                                    |
| **AI Integration**                  | ⚠️ *Ollama (Llama 3.2 3B)* - Local LLM for basic diabetes risk assessment                                                                                                                            |



---

### 🔒 Security implementation

![security_flow.png](_img/security_flow.png)

<details>
<summary>Security versions and associated Git branches (click to expand)</summary>

| Branch | Description | Status                                                                                                                           |
|--------|-------------|----------------------------------------------------------------------------------------------------------------------------------|
| `jwt-header` | JWT Access Token in Authorization header only | ✅                                                                                                                                |
| `access-header-refresh-httponly` | Access token in header + Refresh token in HttpOnly cookie | ✅                                                                                                                                |
| `all-httponly` | Full HttpOnly for Access & Refresh tokens + CSRF token | ❌ *Abandoned*<br/>Too complex for minimal security gain. Modern SPA setups with SameSite and CORS provide sufficient protection. |
| `oauth2-access&refresh` | OAuth2 with Google + classic login (Access & Refresh tokens for both) | ✅ *Current*                                                                                                                      |
| `keycloak` | Keycloak integration | 🕒 *Postponed*                                  |
</details>

---

### 📊 Observability & monitoring

The system includes comprehensive observability to ensure reliability and simplify production diagnostics.

- **Logs**: collected via Alloy, centralized and indexed in Loki
- **Metrics**: exposed by Micrometer, scraped by Prometheus
- **Distributed Traces**: captured by OpenTelemetry, stored in Tempo

All data is visualized and analyzed through Grafana.

The custom dashboard is based on the two popular dashboards : **JVM (Micrometer) & Spring Boot Observability**.

![Custom Observability Dashboard](_img/dashboard-image.png)

It highlights critical KPIs to ensure system health and performance:

| **KPI**                                 | **Description**                                                                                  |
|----------------------------------------|--------------------------------------------------------------------------------------------------|
| **Uptime**                              | Indicates system availability and stability over time                                            |
| **CPU usage (system and process)**      | Monitors resource consumption and detects overloads                                              |
| **Memory usage (heap and non-heap)**    | Tracks JVM memory usage to identify leaks or memory pressure                                     |
| **Request rate (requests per second)**  | Measures traffic volume handled by the service                                                   |
| **Request duration (p99, p95, p50)**    | Captures latency distributions for real user experience insights                                 |
| **Total requests and status codes**     | Tracks success and error rates (e.g. 2xx, 5xx) to monitor reliability                            |
| **Exception counts**                    | Identifies unexpected failures not reflected in HTTP status codes                                |


### 📉 Additional insights

![distributed-tracing-high-risk-event.png](_img/distributed-tracing-high-risk-event.png)


<details>
<summary>Distributed tracing - full flow details matching the screenshot (click to expand)</summary>

| Service | Step                                                     | Description                                                                      |
| --- |----------------------------------------------------------|----------------------------------------------------------------------------------|
| Notes | Create note `(triggers reassessment)`                    | Create a note via POST /notes                                                    |
| API Gateway | Proxy requests                                           | Route requests to Assessments                                                    |
| Assessments (Feign Client) | Fetch patient and notes  `(in parallel)`                 | Fetch patient (GET /patients/{id}) and notes (GET /notes?patientId={id})         |
| Assessments | Assess risk                                              | Calculate risk via generateAssessment (trigger analysis + rules)                 |
| Assessments | Publish event                                            | If risk = "Early onset", publish high-risk-assessment event to RabbitMQ          |
| Assessments (Feign Client) | Update patient flag `(prevent sending duplicate emails)` | Update patient's earlyOnsetMailSent flag via PUT /patients/{id}/early-onset-mail |
| Notifications | Consume event and send email                             | Consume high-risk-assessment event and send alert email via Mailtrap             |

</details>

<details>
<summary>Other example - synchronous vs asynchronous feign calls for ms assessments (click to expand)</summary>

**Synchronous (Sequential – 41 ms)**
![Synchronous](_img/synchronous-assessment-feign-calls.png)
**Asynchronous (Parallel – 26 ms)**
![Asynchronous](_img/asynchronous-assessment-feign-calls.png)
</details>

---

### 🧪 Testing strategy

#### ✅ Unit & Integration tests

- Implemented for: **Patients**, **Notes**, **Assessments** and the **Gateway**
- Covers core business logic, database operations, and Feign communication

#### ✅ End-to-End (E2E) tests

The full journey test simulates a real doctor's workflow using `DoctorJourneyE2ETest`:
- Verifies patient creation, note insertion and risk assessment logic
- Covers a 4-step risk evolution: *None → Borderline → In Danger → Early onset*
- Confirms high-risk email delivery by checking the notifications service logs
- Validates data flow across all services
- Uses **Awaitility** to ensure service readiness and propagation
- Executed in a real environment with **Docker Compose**

#### ✅ Performance tests (in progress)

Each test is launched dynamically via environment variables, enabling modular and reproducible testing with different test types and performance profiles. (using `k6` via a dedicated Docker Compose)

**Example command:**

```bash
TEST_TYPE=realistic TEST_PROFILE=load docker-compose -f docker-compose-perf-k6.yml up 
```

<details open>
<summary>📄 1. Load testing</summary>

>| **Item**             | **Description**                                                                                                                                                                                                                                                                           |
>|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
>| Test profile         | [load] <br>- reaching up to 160 virtual users, then ramping down <br>- total time : 11 minutes<br>- thresholds: <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - 95% of requests complete in under 2000 milliseconds <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - less than 5% of requests fail |
>| Test type (scenario) | [realistic-traffic] <br>- login <br>- patient list view (home page) <br>- patient record <br>- patient creation <br>- simple note creation <br>- critical note creation                                                                                                                   |
>| Goal                 | Measure Gateway performance under a realistic load, simulating <u>a number and pace of users close to a normal usage</u>, with and without monitoring.                                                                                                                                    |
>| Date                 | [YYYY-MM-DD]                                                                                                                                                                                                                                                                              |
>
>
> ##### Key Results
>
> | KPI                   | Without monitoring | With monitoring |
> |-----------------------|--------------------|-----------------|
> | Avg response time     | [X] ms             | [X] ms          |
> | 95th percentile (p95) | [X] ms             | [X] ms          |
> | Request rate          | [X] req/s          | [X] req/s       |
> | Error rate            | [X]%               | [X]%            |
> | Completed iterations  | [X]                | [X]             |
>
>
> ##### Dashboard Overview (live monitoring)
>
> ![load testing results](../_img/load-testing-results.png)

</details>

<details>
<summary>📄 2. Stress testing</summary>

> | **Item**             | **Description**                                                                                                                                                                                                                                                                              |
> |----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
> | Test profile         | [stress] <br>- reaching up to 400 virtual users, then ramping down <br>- total time: 11 minutes <br>- thresholds: <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - 95% of requests complete in under 8000 milliseconds <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - less than 20% of requests fail |
> | Test type (scenario) | [realistic-traffic] <br>- login <br>- patient list view (home page) <br>- patient record <br>- patient creation <br>- simple note creation <br>- critical note creation                                                                                                                      |
> | Goal                 | Assess Gateway stability and performance limits under <u>increasing load, pushing beyond normal usage</u>, with and without monitoring.                                                                                                                                                      |
> | Date                 | [YYYY-MM-DD]                                                                                                                                                                                                                                                                                 |
>
> ##### Key Results
>
> | KPI                   | Without monitoring | With monitoring |
> |-----------------------|--------------------|-----------------|
> | Avg response time     | [X] ms             | [X] ms          |
> | 95th percentile (p95) | [X] ms             | [X] ms          |
> | Request rate          | [X] req/s          | [X] req/s       |
> | Error rate            | [X]%               | [X]%            |
> | Completed iterations  | [X]                | [X]             |
>
> ##### Dashboard Overview (live monitoring)
> 
> ![stress testing results](../_img/stress-testing-results.png)

</details>

<details>
<summary>📄 3. Spike testing</summary>

> | **Item**             | **Description**                                                                                                                                                                                                                                                                                                 |
> |----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
> | Test profile         | [spike] <br>- sudden jump to 400 virtual users, short bursts, then quick ramp down <br>- total time: ~6.5 minutes <br>- thresholds: <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - 95% of requests complete in under 10000 milliseconds <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - less than 30% of requests fail |
> | Test type (scenario) | [realistic-traffic] <br>- login <br>- patient list view (home page) <br>- patient record <br>- patient creation <br>- simple note creation <br>- critical note creation                                                                                                                                         |
> | Goal                 | Evaluate Gateway’s capacity to handle <u>sudden traffic spikes</u> and recovery behavior, with and without monitoring.                                                                                                                                                                                          |
> | Date                 | [YYYY-MM-DD]                                                                                                                                                                                                                                                                                                    |
>
> ##### Key Results
>
> | KPI                   | Without monitoring | With monitoring |
> |-----------------------|--------------------|-----------------|
> | Avg response time     | [X] ms             | [X] ms          |
> | 95th percentile (p95) | [X] ms             | [X] ms          |
> | Request rate          | [X] req/s          | [X] req/s       |
> | Error rate            | [X]%               | [X]%            |
> | Completed iterations  | [X]                | [X]             |
>
> ##### Dashboard Overview (live monitoring)
>
> ![spike testing results](../_img/spike-testing-results.png)

</details>

<details>
<summary>📄 4. Soak testing</summary>

> | **Item**             | **Description**                                                                                                                                                                                                                                                                            |
> |----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
> | Test profile         | [soak] <br>- steady load of 40 virtual, ramp up and down included <br>- total time: 64 minutes <br>- thresholds: <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - 95% of requests complete in under 3000 milliseconds <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - less than 5% of requests fail | 
> | Test type (scenario) | [realistic-traffic] <br>- login <br>- patient list view (home page) <br>- patient record <br>- patient creation <br>- simple note creation <br>- critical note creation                                                                                                                    |
> | Goal                 | Verify Gateway stability and <u>resource usage under sustained load over an extended period</u>, with and without monitoring.                                                                                                                                                              |
> | Date                 | [YYYY-MM-DD]                                                                                                                                                                                                                                                                               |
>
> ##### Key Results
>
> | KPI                   | Without monitoring | With monitoring |
> |-----------------------|--------------------|-----------------|
> | Avg response time     | [X] ms             | [X] ms          |
> | 95th percentile (p95) | [X] ms             | [X] ms          |
> | Request rate          | [X] req/s          | [X] req/s       |
> | Error rate            | [X]%               | [X]%            |
> | Completed iterations  | [X]                | [X]             |
>
> ##### Dashboard Overview (live monitoring)
>
> ![soak testing results](../_img/soak-testing-results.png)

</details>

- A full analysis was conducted to understand the system's saturation behavior.  
  👉 [Read the full performance analysis](_doc/performance-analysis.md)

---

### 🔔 Event-driven

The system implements asynchronous communication using **RabbitMQ** for critical notifications:

- **High-Risk assessment events**: when a patient is assessed as `"Early onset"`, the **Assessments** service publishes an event to the `high-risk-assessments` queue
- **No duplicates**: the alert is triggered only when the risk changes to `"Early onset"`
- **Email notifications**: the **Notifications** service consumes these events and sends automated email alerts to healthcare providers (emails are intercepted using **Mailtrap** during development)

---

### 🚀 CI/CD pipelines

- `push_dev_ci.yml`: runs unit tests on modified microservices when pushing to `dev`
- `pr_main_ci-cd.yml`: builds, tests, generates JaCoCo & JavaDocs, deploys docs to GitHub Pages (on PR to `main`)
- `merge_main_cd.yml`: pushes Docker images to Docker Hub after PR is merged into `main`

---

### ❌ Out of scope

- **Spring Cloud Config Server**: no centralized configuration management. *(used in a different project, with RabbitMQ as the refresh trigger and a GitHub repository for versioning and storing configurations)*
- **Secrets manager**: secrets are managed via environment variables.
- **Front-end testing** : deprioritized to focus efforts on back-end reliability and service integration.