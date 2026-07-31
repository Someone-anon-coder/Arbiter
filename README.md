# Arbiter — Java Learning Journey & Microservices Build

Welcome to Arbiter — a documented, hands-on journey from Java fundamentals to a deployed microservices platform.

This repository follows the same spirit as my [Python](https://github.com/Someone-anon-coder/Python) and [Golang](https://github.com/Someone-anon-coder/Golang) learning-journey repos: start from the basics, progress deliberately toward advanced, production-relevant concepts, and prove each concept hands-on before moving on. The twist here is the learning model itself — see below.

---

## How This Repository Works

Every concept is taught, not handed over:

1. **Explain** — what the concept is, how it's done, its structure.
2. **Two problems** — one simple, one hard — set fresh for that concept.
3. **I solve both, independently.** No solution code is ever written for me.
4. **Evaluate** — correctness first, then improvements *only if genuinely warranted* (not manufactured), plus a basic explanation of the reasoning.

This applies to both phases of the repo:

- **Daily Learning Phase** — one Java concept at a time, from basics through advanced backend engineering, following `ROADMAP.md`.
- **Project Build Phase** — once the fundamentals are covered, the same protocol applies to building **Arbiter** itself: one small, independently testable increment at a time, per `PROJECT_PLAN.md`.

Claude Code acts as instructor and evaluator throughout — never as implementer. Every line of code in this repository, in both phases, is written by me.

---

## What Gets Built: Arbiter

Arbiter is a microservices-based test management and execution platform — think a stripped-down TestRail/Jira — covering:

- REST APIs across multiple independent services (Auth, Test Design, Execution, Defect, Notification, API Gateway)
- Relational database handling per service (PostgreSQL + Spring Data JPA)
- Synchronous (REST) and asynchronous (message broker) inter-service communication
- JWT authentication and role-based access control
- Resilience patterns (circuit breakers, retries)
- Caching (Redis)
- A full testing stack: JUnit 5, Mockito, AssertJ, Testcontainers, REST Assured
- Containerization (Docker, Docker Compose)
- Deployment to AWS free tier (EC2 + RDS)

Full architecture is documented in `PROJECT_PLAN.md` — locked until the Daily Learning Phase is substantially complete, so concepts are learned on their own merits first.

---

## 📂 Repository Structure

```
.
├── CLAUDE.md                  # Governing rules for every Claude Code session
├── CURRENT_PROGRESS.md        # Live log: what's been covered, how it went, what's next
├── PROJECT_PLAN.md            # 🔒 Locked — Arbiter architecture & build blueprint
├── ROADMAP.md                 # Checkbox-style learning path (visible, in-progress plan)
├── README.md                  # This file
│
├── Explanations/              # Per-concept written explanations, paired with each numbered file
├── Files/                     # Input/output artifacts generated while working through concepts
├── Database/                  # Local DB files/migrations used during the learning phase
├── Packages/                  # Custom packages/utilities built along the way
├── Images/                    # Diagrams referenced in explanations
├── arbiter/                   # The actual Arbiter multi-service project (Project Build Phase)
│
├── java_1Basics.java
├── java_2ControlFlow.java
├── java_3Methods.java
├── ...                        # One file per concept, numbered in learning order
│
└── pom.xml / build files
```

---

## ⚙️ How to Use This Repository

```bash
git clone https://github.com/Someone-anon-coder/Java.git
cd Java
```

Each numbered concept file is self-contained and runnable on its own:

```bash
javac java_1Basics.java
java java_1Basics
```

Once the Project Build Phase begins, `arbiter/` becomes a proper Maven multi-module project with its own build/run instructions (added at that point).

---

## 🎯 Goals

- Build real command of Java — from language fundamentals to backend engineering — through deliberate practice, not passive tutorial-following.
- Close the gap between "has read about microservices" and "has built, tested, and deployed one."
- Produce a portfolio piece (Arbiter) directly relevant to test-automation/AI-in-QA roles.
- Get hands-on with AWS free-tier deployment in small, understood steps rather than a black-box `deploy` command.

## 📈 Progress Tracking

See `CURRENT_PROGRESS.md` for the live, detailed log, and `ROADMAP.md` for the full topic checklist.

- ✅ Complete · 🟡 In Progress · ⬜ Not Started · 🔒 Locked (Project Build Phase, pending Daily Learning completion)

## 🔗 Resources

- [Oracle Java Documentation](https://docs.oracle.com/en/java/)
- [Spring Documentation](https://docs.spring.io/spring-framework/reference/)
- [Baeldung](https://www.baeldung.com/)

## 📬 Contact

Feedback, suggestions, and corrections are welcome — feel free to open an issue or pull request.

Happy coding! ☕