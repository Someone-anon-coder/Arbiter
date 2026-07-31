# CURRENT_PROGRESS.md
### Live Progress Log — Arbiter Learning & Build

This file is the continuity thread between sessions. Claude Code reads it at the start of every session and updates it at the end. Claude (chat) reads it before writing the next session's prompt.

---

## Status Overview

| Field | Value |
|---|---|
| Current Phase | Daily Learning Phase |
| Project Build Phase Unlocked | No |
| Current Roadmap Category | 1 — Basics of Java |
| Current Concept | Complete |
| Concepts Completed | 1 / 32 |
| Sessions Completed | 1 |
| Start Date | 2026-07-31 |
| Days Elapsed | 0 |
| Target Duration | 25–30 days |

---

## How to Use This File

- **Claude Code**: read the Status Overview and the most recent Session Log entry before starting anything. Append a new Session Log entry at the end of every session — do not edit past entries except to correct a factual error.
- **Claude (chat)**: use this file to determine what the next session's prompt should cover, and to spot recurring struggle points worth addressing directly in a future prompt.
- **Aayush**: this is the visible record of what's actually been covered and how it went — useful for spotting patterns, like a concept that needed a second pass.

---

## Session Log

*Most recent session first. Copy the template below for each new entry.*

**Template:**
```
### Session N — [Date]
- Phase: Daily Learning / Project Build
- Concept / Build Step:
- Problems given: (simple) ___ / (hard) ___
- Outcome: Correct / Correct with noted improvement / Needed a second attempt
- Evaluation summary:
- Struggles / notes:
- Next session should cover:
```

### Session 1 — 2026-07-31
- Phase: Daily Learning
- Concept / Build Step: Category 1 — Basics of Java (JDK/environment, variables & data types, operators, type conversion/casting, I/O)
- Problems given: (simple) declare 4 primitive types incl. int/float/char/boolean, assign literals, print with labels / (hard) Scanner-driven program reading name/age/height, converting height to meters, computing age+10, printing a combined summary line
- Outcome: Correct
- Evaluation summary: Both problems correct on first submission, verified by compiling and running with sample input. Notable: solved the nextInt()/nextLine() buffer pitfall by reading all input via nextLine() + parseInt/parseDouble rather than an extra flush call — a clean alternative, and the commented-out nextInt() attempt showed the pitfall was understood, not avoided by luck. Minor note (not an error): cast target in the height conversion was narrower (float) than necessary given a double source — worth remembering to cast only when the language requires it, not just to demonstrate a cast.
- Struggles / notes: None. Also caught and fixed a repo documentation bug: README.md's stated file-naming convention (`java_1-basics.java`) used a hyphen, which is not a legal Java identifier character — file/class names can't compile with one. Convention corrected repo-wide to digit+PascalCase (`java_1Basics.java`) to match the author's actual (correct) file name; author self-caught this before submitting.
- Next session should cover: Category 2 — Control Flow (if/else/else if, switch classic and arrow form, for/while/do-while, break/continue/labeled loops)

*(Prior sessions: none.)*

---

## Concept Completion Table

Mirrors `ROADMAP.md`. Status values: ⬜ Not Started · 🟡 In Progress · ✅ Complete. This table is the source of truth for "Concepts Completed" above — keep both in sync.

| # | Concept | Status | Date Completed |
|---|---|---|---|
| 1 | Basics of Java | ✅ | 2026-07-31 |
| 2 | Control Flow | ⬜ | |
| 3 | Methods | ⬜ | |
| 4 | OOP — Core | ⬜ | |
| 5 | OOP — Advanced | ⬜ | |
| 6 | Arrays & Core Collections | ⬜ | |
| 7 | Strings & Text Processing | ⬜ | |
| 8 | Exception Handling | ⬜ | |
| 9 | Generics | ⬜ | |
| 10 | Java 8+ Functional Features | ⬜ | |
| 11 | Multithreading & Concurrency | ⬜ | |
| 12 | File I/O & NIO | ⬜ | |
| 13 | Data Structures & Algorithms | ⬜ | |
| 14 | Build Tools & Project Structure | ⬜ | |
| 15 | JDBC & Relational DB Basics | ⬜ | |
| 16 | Testing Fundamentals (JUnit 5) | ⬜ | |
| 17 | Advanced Testing (Mockito, AssertJ, Testcontainers) | ⬜ | |
| 18 | Spring Core (IoC & DI) | ⬜ | |
| 19 | Spring Boot Fundamentals | ⬜ | |
| 20 | Building REST APIs | ⬜ | |
| 21 | Spring Data JPA & DB Handling | ⬜ | |
| 22 | API-Level Testing | ⬜ | |
| 23 | Security & Authentication | ⬜ | |
| 24 | Advanced Backend Concepts | ⬜ | |
| 25 | Caching | ⬜ | |
| 26 | Asynchronous Messaging | ⬜ | |
| 27 | Microservices Concepts | ⬜ | |
| 28 | Resilience Patterns | ⬜ | |
| 29 | Containerization | ⬜ | |
| 30 | Observability & Health | ⬜ | |
| 31 | CI/CD Basics | ⬜ | |
| 32 | AWS Fundamentals for Deployment | ⬜ | |

---

## Recurring Issues / Skill Gaps

*(None logged yet. Claude Code should note here if the same type of mistake shows up across multiple sessions — e.g., consistently missing null checks, or a recurring misunderstanding of a specific concept — so the Curriculum Architect can address it directly instead of it silently resurfacing.)*

---

## Project Build Phase Log

*(Not started. Once Phase 2 unlocks, build-step entries follow the same Session Log format above, referencing the step number from `PROJECT_PLAN.md` Section 8.)*

| Step | Description | Status | Date Completed |
|---|---|---|---|
| 1–17 | See `PROJECT_PLAN.md` Section 8 (locked until Daily Learning Phase is complete) | 🔒 Locked | |

---

## Next Session

**Next up:** Concept #2 — Control Flow (see `ROADMAP.md`).
