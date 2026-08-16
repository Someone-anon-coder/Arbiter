# ROADMAP.md
### Arbiter Learning Path — Java Fundamentals to Deployed Microservices

This is the visible plan of topics, in order. `CURRENT_PROGRESS.md` tracks what's actually been done and how it went — this file tracks what's *supposed* to happen and when it's checked off.

Legend: ✅ Complete · 🟡 In Progress · ⬜ Not Started

---

## 1. Basics of Java ✅
- [x] Setting up JDK, JAVA_HOME, running a first program
- [x] Variables and data types (primitives vs. reference types)
- [x] Operators
- [x] Type conversion / casting
- [x] Input / Output (Scanner, System.out)

## 2. Control Flow ✅
- [x] `if`, `else`, `else if`
- [x] `switch` (classic and enhanced/arrow form)
- [x] `for`, `while`, `do-while` loops
- [x] `break`, `continue`, labeled loops

## 3. Methods ✅
- [x] Method declaration, parameters, return values
- [x] Method overloading
- [x] Varargs
- [x] Recursion
- [x] Pass-by-value semantics in Java

## 4. Object-Oriented Programming — Core ✅
- [x] Classes and objects
- [x] Constructors, `this`
- [x] Encapsulation (access modifiers, getters/setters)
- [x] Static vs. instance members

## 5. Object-Oriented Programming — Advanced ✅
- [x] Inheritance
- [x] Polymorphism (overriding, dynamic dispatch)
- [x] Abstraction (abstract classes)
- [x] Interfaces (including default/static methods)
- [x] `equals()`, `hashCode()`, `toString()`

## 6. Arrays & Core Collections ✅
- [x] Arrays (1D, 2D, multi-dimensional)
- [x] `ArrayList`, `LinkedList`
- [x] `HashMap`, `TreeMap`, `LinkedHashMap`
- [x] `HashSet`, `TreeSet`
- [x] Iterating collections, `Iterator`
- [x] Choosing the right collection for a problem

## 7. Strings & Text Processing ✅
- [x] String immutability, `String` vs `StringBuilder` vs `StringBuffer`
- [x] Common `String` methods
- [x] String formatting
- [x] Regular expressions (`Pattern`, `Matcher`)

## 8. Exception Handling ✅
- [x] `try`, `catch`, `finally`
- [x] Checked vs. unchecked exceptions
- [x] Custom exceptions
- [x] Try-with-resources

## 9. Generics ✅
- [x] Generic classes and methods
- [x] Bounded type parameters
- [x] Wildcards (`? extends`, `? super`)

## 10. Java 8+ Functional Features ⬜
- [ ] Lambda expressions
- [ ] Functional interfaces (`Function`, `Predicate`, `Consumer`, `Supplier`)
- [ ] Streams API (map, filter, reduce, collect)
- [ ] Method references
- [ ] `Optional`

## 11. Multithreading & Concurrency ⬜
- [ ] `Thread` and `Runnable`
- [ ] `synchronized`, locks
- [ ] `ExecutorService` and thread pools
- [ ] `CompletableFuture`
- [ ] Common concurrency pitfalls (race conditions, deadlocks)

## 12. File I/O & NIO ⬜
- [ ] Reading/writing files (classic I/O)
- [ ] `java.nio.file` (Path, Files)
- [ ] Working with JSON/CSV in Java

## 13. Data Structures & Algorithms ⬜
- [ ] Stacks, Queues
- [ ] Linked Lists (custom implementation)
- [ ] Trees, Graphs (basics)
- [ ] Sorting and searching algorithms
- [ ] Big-O reasoning applied to the above

## 14. Build Tools & Project Structure ⬜
- [ ] Maven fundamentals (`pom.xml`, dependencies, lifecycle)
- [ ] Multi-module project structure
- [ ] Dependency management basics

## 15. JDBC & Relational Database Basics ⬜
- [ ] JDBC connections, `Statement` vs `PreparedStatement`
- [ ] CRUD via raw JDBC
- [ ] Connection pooling concepts
- [ ] SQL fundamentals as needed (joins, constraints, indexes)

## 16. Testing Fundamentals (JUnit 5) ⬜
- [ ] Test structure, annotations (`@Test`, `@BeforeEach`, etc.)
- [ ] Assertions
- [ ] Parameterized tests
- [ ] Test lifecycle and organization

## 17. Advanced Testing (Mockito, AssertJ, Testcontainers) ⬜
- [ ] Mocking with Mockito
- [ ] Fluent assertions with AssertJ
- [ ] Integration testing with Testcontainers (real Postgres)
- [ ] Test doubles: mock vs. stub vs. spy — when to use which

## 18. Spring Core (IoC & DI) ⬜
- [ ] Inversion of Control, Dependency Injection
- [ ] Beans, application context
- [ ] Annotation-based configuration

## 19. Spring Boot Fundamentals ⬜
- [ ] Auto-configuration, starters
- [ ] `application.properties` / `application.yml`
- [ ] Profiles

## 20. Building REST APIs ⬜
- [ ] `@RestController`, request mapping
- [ ] DTOs and request/response separation
- [ ] Validation (`@Valid`, Bean Validation)
- [ ] Global exception handling (`@ControllerAdvice`)

## 21. Spring Data JPA & Database Handling ⬜
- [ ] Entities, repositories
- [ ] Relationships (`@OneToMany`, `@ManyToOne`, etc.)
- [ ] Transactions (`@Transactional`)
- [ ] Query methods, JPQL basics

## 22. API-Level Testing ⬜
- [ ] `MockMvc` for controller tests
- [ ] REST Assured for black-box API tests
- [ ] Testing happy paths and failure paths

## 23. Security & Authentication ⬜
- [ ] Spring Security fundamentals
- [ ] JWT issuance and validation
- [ ] Role-based access control

## 24. Advanced Backend Concepts ⬜
- [ ] Pagination and filtering
- [ ] API versioning
- [ ] Idempotency basics
- [ ] Consistent error response design

## 25. Caching ⬜
- [ ] Redis fundamentals
- [ ] Spring's caching abstraction (`@Cacheable`, etc.)
- [ ] Cache invalidation basics

## 26. Asynchronous Messaging ⬜
- [ ] Message broker fundamentals (Kafka or RabbitMQ)
- [ ] Producers and consumers
- [ ] Event-driven communication patterns

## 27. Microservices Concepts ⬜
- [ ] Service boundaries and ownership
- [ ] Sync (REST client / `WebClient`) vs. async communication
- [ ] API Gateway pattern
- [ ] Config management basics

## 28. Resilience Patterns ⬜
- [ ] Circuit breakers (Resilience4j)
- [ ] Retries and timeouts
- [ ] Fallback strategies

## 29. Containerization ⬜
- [ ] Docker fundamentals, Dockerfile for a Spring Boot app
- [ ] Docker Compose for multi-service local environments
- [ ] Image size / layering basics

## 30. Observability & Health ⬜
- [ ] SLF4J/Logback logging practices
- [ ] Spring Boot Actuator, health checks
- [ ] Basic metrics awareness

## 31. CI/CD Basics ⬜
- [ ] GitHub Actions fundamentals for a Java/Maven project
- [ ] Running tests in CI
- [ ] Basic build/package pipeline

## 32. AWS Fundamentals for Deployment ⬜
- [ ] EC2 basics (instances, security groups)
- [ ] RDS basics (Postgres free tier)
- [ ] IAM basics as needed for deployment
- [ ] Free-tier limits and cost awareness

---

## 33. Project Build Phase — Arbiter 🔒
Unlocks `PROJECT_PLAN.md`. See that file's Section 8 for the full phased build order (17 steps, Auth → Test Design → Execution → Defect → Notification → Gateway → end-to-end → deployment).

## 34. Deployment Phase — Arbiter on AWS 🔒
Final step of the Project Build Phase — see `PROJECT_PLAN.md` Section 9.

---

## Notes
- Categories 1–17 are core Java + testing fundamentals; 18–32 are backend/Spring/infrastructure concepts. The split roughly separates "language mastery" from "backend engineering," though sessions may interleave where it makes sense.
- Checkboxes here are updated once a category is fully complete (all sub-items ✅). `CURRENT_PROGRESS.md` is the finer-grained, session-by-session record.
