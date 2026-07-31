# PROJECT_PLAN.md
### Arbiter — Architecture & Build Blueprint

> ## 🔒 LOCKED
> **This file is locked.** It must not be opened, read, summarized, or referenced — by Claude Code, or indirectly through questions like "how would this concept apply to the real project?" — during the Daily Learning Phase. It unlocks only when `CURRENT_PROGRESS.md` shows the Daily Learning Phase is sufficiently complete and Aayush explicitly says to proceed to the Project Build Phase.
>
> This exists so concepts are learned for their own sake first, not reverse-engineered toward a known target design.

---

## 1. Overview

**Arbiter** is a microservices-based test management and execution platform — a stripped-down TestRail/Jira hybrid, purpose-built to exercise the full range of Java backend concepts: REST APIs, relational database handling, inter-service communication (sync and async), authentication/authorization, resilience patterns, caching, containerization, and a full testing stack (unit, integration, API-level).

It is built by Aayush, entirely, in small independently testable increments, once the Daily Learning Phase has covered the prerequisite concepts.

---

## 2. Service Breakdown

### 2.1 Auth Service
- Owns: users, roles (`ADMIN`, `QA`, `DEV`), authentication.
- Responsibilities: registration, login, JWT issuance and validation, role assignment.
- Own database: `auth_db` (Postgres).

### 2.2 Test Design Service
- Owns: projects, test suites, test cases.
- Responsibilities: CRUD for projects/suites/cases, test case versioning (basic — a version number bumped on edit, not full diffing).
- Own database: `test_design_db`.

### 2.3 Execution Service
- Owns: test runs, individual case results (pass/fail/skip/blocked).
- Responsibilities: start a run against a suite, record per-case results, compute run summary stats.
- References Test Design Service data by ID, via a synchronous REST call — not a shared database.
- Own database: `execution_db`.

### 2.4 Defect Service
- Owns: defects/bugs.
- Responsibilities: create/update/close defects, link a defect to a failed execution result.
- Own database: `defect_db`.

### 2.5 Notification Service
- Owns: nothing persistent (or minimal — a log of sent notifications).
- Responsibilities: consumes "execution failed" events (async, via message broker) and sends an alert. A real email is not required — a logged or mocked send is sufficient for the learning goal.
- This is the service that introduces async messaging into the architecture.

### 2.6 API Gateway
- Single entry point for all client requests; routes to the appropriate downstream service.
- Handles cross-cutting concerns at the edge: JWT validation pass-through, basic rate limiting (stretch goal, not required for MVP).

---

## 3. Data Model (high-level, per service)

**Auth Service**
- `User(id, username, email, password_hash, role, created_at)`

**Test Design Service**
- `Project(id, name, description, created_at)`
- `TestSuite(id, project_id, name, description)`
- `TestCase(id, suite_id, title, steps, expected_result, version, created_at, updated_at)`

**Execution Service**
- `TestRun(id, suite_id, project_id, triggered_by_user_id, started_at, completed_at, status)`
- `TestResult(id, run_id, test_case_id, status, actual_result, executed_at)`

**Defect Service**
- `Defect(id, title, description, severity, status, linked_result_id, created_by, created_at)`

*(Field lists are a starting point, not a frozen schema — expect to add fields as build steps surface real needs.)*

---

## 4. Inter-Service Communication

- **Synchronous (REST)**: Execution Service → Test Design Service (fetch suite/case details when starting a run). Defect Service → Execution Service (fetch result details when linking a defect). Use Spring's `RestTemplate` or `WebClient`, whichever the Daily Learning Phase covers.
- **Asynchronous (messaging)**: Execution Service publishes an `ExecutionFailed` event on a message broker (Kafka or RabbitMQ — decided based on which the roadmap covers) when a test result is marked failed. Notification Service subscribes and reacts.
- **API Gateway → all services**: synchronous REST routing.

---

## 5. Tech Stack

| Concern | Choice |
|---|---|
| Language | Java (LTS version, matched to whatever the learning phase standardizes on) |
| Framework | Spring Boot |
| Build tool | Maven |
| Database | PostgreSQL (one logical database per service) |
| ORM | Spring Data JPA |
| Auth | Spring Security + JWT |
| Caching | Redis (used for at least one clearly justified case — e.g., test suite lookups on the Execution Service) |
| Messaging | Kafka or RabbitMQ (one, chosen during the relevant roadmap concept) |
| Resilience | Resilience4j (circuit breaker + retry on the Execution → Test Design sync call) |
| Containerization | Docker; Docker Compose to run all services + Postgres + Redis + broker locally |
| Unit testing | JUnit 5 + Mockito + AssertJ |
| Integration testing | Testcontainers (real Postgres in tests, not H2) |
| API testing | REST Assured and/or MockMvc |
| Deployment | AWS free tier — EC2 (Docker Compose) + RDS free tier for Postgres |

---

## 6. Resilience & Cross-Cutting Concerns

- Global exception handling per service (`@ControllerAdvice`), consistent error response shape across all services.
- Circuit breaker + retry on at least the Execution → Test Design sync call, to make the pattern concrete rather than theoretical.
- SLF4J/Logback logging in every service (log aggregation is out of scope for MVP).
- Health checks via Spring Boot Actuator on every service.

---

## 7. Testing Strategy

Every build step in Section 8 ships with tests before it's considered done:
- **Unit tests** for service/business logic (Mockito for dependencies).
- **Integration tests** for the repository/DB layer (Testcontainers — real Postgres, not an in-memory substitute).
- **API-level tests** for controller endpoints (REST Assured or MockMvc) — at minimum the happy path and one failure path per endpoint.

No build step is "done" without its tests passing. This is a hard requirement, not a nice-to-have — it is a core reason this project exists.

---

## 8. Build Order (phased, small independently testable increments)

Each numbered item is one Project Build Phase session (see `CLAUDE.md` Section 5). The order is designed so each step produces something independently runnable and testable before the next begins.

1. Repo/multi-module skeleton + Docker Compose shell (Postgres containers only, services empty) — verify `docker compose up` brings up empty Postgres instances.
2. Auth Service: `User` entity + registration endpoint + unit/integration tests.
3. Auth Service: login endpoint + JWT issuance + tests.
4. Auth Service: JWT validation filter + role-based endpoint protection + tests.
5. Test Design Service: `Project` CRUD endpoints + tests.
6. Test Design Service: `TestSuite` CRUD (nested under project) + tests.
7. Test Design Service: `TestCase` CRUD (nested under suite) + versioning field + tests.
8. Execution Service: `TestRun` creation, calling Test Design Service synchronously to validate the suite exists + tests (including a mocked failure of the downstream call).
9. Execution Service: `TestResult` recording per case + run summary computation + tests.
10. Resilience: add circuit breaker + retry to the Execution → Test Design call + tests proving the fallback behavior.
11. Defect Service: `Defect` CRUD + link-to-result endpoint + tests.
12. Messaging: broker setup in Docker Compose + Execution Service publishes `ExecutionFailed` event + test proving the event is published on a failed result.
13. Notification Service: consumes `ExecutionFailed`, sends a mocked/logged notification + tests.
14. Caching: Redis integration on a clearly-justified read path (e.g., suite lookup) + tests proving cache hit/miss behavior.
15. API Gateway: routing to all services + JWT pass-through + tests.
16. End-to-end smoke test across the full flow (create project → suite → case → run → fail a result → link a defect → confirm notification triggered) — the first point everything runs together.
17. AWS deployment: EC2 provisioning, Docker Compose deployment, RDS free-tier Postgres, verify the same end-to-end flow works against the deployed environment.

*(This order can shift if a step surfaces a real dependency problem — that's expected. Update this section and note the change in `CURRENT_PROGRESS.md` rather than silently deviating.)*

---

## 9. AWS Deployment Plan (Step 17 detail)

- **Compute**: one EC2 free-tier instance (t2.micro/t3.micro, depending on current free-tier terms), running all services via Docker Compose.
- **Database**: RDS free-tier Postgres instance, replacing the local Postgres containers for a "real" deployed feel — or, if RDS free-tier limits make one instance per service impractical, a single RDS instance with five schemas/databases logically separated.
- **Networking**: security groups scoped to only the ports actually needed; no broad open access.
- **Explicitly out of scope for MVP deployment**: EKS/ECS, auto-scaling, multi-AZ, managed Kafka (MSK) — these cost money beyond free tier and aren't needed to demonstrate the concept.

---

## 10. Explicit Non-Goals

To keep this achievable in the time remaining after the learning phase:
- No frontend/UI — API-only, verified via REST Assured/Postman/curl.
- No full audit-log/versioning system for test cases — a simple version counter is sufficient.
- No production-grade secrets management — environment variables are acceptable for this learning project (not a pattern to carry into an actual production system without addressing it separately).
- No horizontal scaling or load testing of Arbiter itself — a possible future extension, not part of this build.
