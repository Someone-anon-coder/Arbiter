# CURRENT_PROGRESS.md
### Live Progress Log — Arbiter Learning & Build

This file is the continuity thread between sessions. Claude Code reads it at the start of every session and updates it at the end. Claude (chat) reads it before writing the next session's prompt.

---

## Status Overview

| Field | Value |
|---|---|
| Current Phase | Daily Learning Phase |
| Project Build Phase Unlocked | No |
| Current Roadmap Category | 4 — OOP Core |
| Current Concept | Complete |
| Concepts Completed | 4 / 32 |
| Sessions Completed | 4 |
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

### Session 4 — 2026-08-01
- Phase: Daily Learning
- Concept / Build Step: Category 4 — OOP Core (classes/objects, constructors, `this`, encapsulation/access modifiers/getters-setters, static vs. instance members)
- Problems given: (simple) `Book` class with private fields, constructor, getters, and a `setPages` setter that rejects values < 1 without crashing / (hard) `BankAccount` class combining instance state (owner, balance, auto-assigned sequential id) with static state (shared running total balance across all accounts, static id counter), requiring deposit/withdraw to keep both in sync and a rejected over-withdrawal to change nothing
- Outcome: Correct
- Evaluation summary: Both problems correct on first submission, verified by compiling and running. `Book`'s two instances stayed fully independent, and the rejected `-5` setter call left the original value (37) untouched. `BankAccount` correctly separated instance fields from static fields: account ids were unique/sequential via a static counter, and the static running total was proven to track combined state across all three instances (not just one) — after a deposit, a successful withdrawal, and a rejected over-withdrawal, the printed static total (9000.0) matched the hand-summed individual balances (3000 + 1000 + 5000). No rework needed.
- Struggles / notes: None.
- Next session should cover: Category 5 — OOP Advanced (inheritance, polymorphism, abstraction/abstract classes, interfaces including default/static methods, `equals()`/`hashCode()`/`toString()`)

### Session 3 — 2026-08-01
- Phase: Daily Learning
- Concept / Build Step: Category 3 — Methods (declaration/parameters/return values, overloading, varargs, recursion, pass-by-value semantics)
- Problems given: (simple) overloaded `describe` method (single int / two ints / String label + int varargs, including zero-arg and 5-arg calls) / (hard) recursive `flattenAndSum(int[][] grid, int[] runningMax)` over a jagged 2D array with no loops permitted, requiring a helper recursive method, plus a required demonstration of reassigning the array parameter (`flattenAndSumBroken`) to prove the caller's array is unaffected
- Outcome: Correct
- Evaluation summary: Both problems correct on first submission, verified by compiling and running (sum = 23, max = 9, hand-checked). Recursion used two cooperating private helper methods (row recursion calling column recursion) with zero loops anywhere, satisfying the constraint. The pass-by-value demonstration was genuinely proven, not just asserted: `flattenAndSumBroken` reassigned its `runningMax` parameter to a new array, and the printed output confirmed the caller's original array in `main` still held `Integer.MIN_VALUE` afterward — correctly distinguishing "mutate through the reference" from "reassign the parameter" for reference types.
- Struggles / notes: None.
- Next session should cover: Category 4 — OOP Core (classes and objects, constructors, `this`, encapsulation/access modifiers/getters-setters, static vs. instance members)

### Session 2 — 2026-07-31
- Phase: Daily Learning
- Concept / Build Step: Category 2 — Control Flow (if/else/else if, switch classic and arrow form, for/while/do-while, break/continue/labeled loops)
- Problems given: (simple) score-to-letter-grade via if/else if/else, run across all five grade bands / (hard) 4x4 grid scan using nested for loops + switch on cell code + labeled break to halt both loops the instant a target cell is found
- Outcome: Correct
- Evaluation summary: Both problems correct on first submission, verified by compiling and running. Problem 1 covered all five grade bands twice over (array sweep + commented-out single-value alternates). Problem 2's labeled break was proven to actually work, not just compile: target sat at (2,3) with an unscanned obstacle-bearing row (row 3) still remaining, and that row never printed — confirming `break found;` exited both the switch and both loops rather than just the switch. Minor cosmetic-only note: output format used "(0,2)" instead of the spec's "(0, 2)" and added an extra leading blank line before the target-found message — noted but not worth a resubmission.
- Struggles / notes: None.
- Next session should cover: Category 3 — Methods (declaration/parameters/return values, overloading, varargs, recursion, pass-by-value semantics)

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
| 2 | Control Flow | ✅ | 2026-07-31 |
| 3 | Methods | ✅ | 2026-08-01 |
| 4 | OOP — Core | ✅ | 2026-08-01 |
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

**Next up:** Concept #5 — OOP Advanced (see `ROADMAP.md`).
