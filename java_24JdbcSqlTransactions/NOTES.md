# Session 24: SQL Joins and Indexes, JDBC Batching, JDBC Transactions — Notes & Verification Evidence

## Environment & Versions

- **PostgreSQL Server Version:** `PostgreSQL 16.15 (Ubuntu 16.15-0ubuntu0.24.04.1) on x86_64-pc-linux-gnu, compiled by gcc (Ubuntu 13.3.0-6ubuntu2~24.04.1) 13.3.0, 64-bit`
- **JDBC Driver Version:** `org.postgresql:postgresql:jar:42.7.14` (`compile` scope)
- **Java Release Target:** JDK 21 (`maven.compiler.release = 21`, verified via class file `major version: 65`, `minor version: 0`)
- **Connection Configuration:** Read exclusively from environment variables `ARBITER_DB_URL`, `ARBITER_DB_USER`, and `ARBITER_DB_PASSWORD` (redacted below).

```text
$ PGPASSWORD="$ARBITER_DB_PASSWORD" psql "${ARBITER_DB_URL#jdbc:}" -U "$ARBITER_DB_USER" -c "SELECT version();"
                                                                version                                                                 
----------------------------------------------------------------------------------------------------------------------------------------
 PostgreSQL 16.15 (Ubuntu 16.15-0ubuntu0.24.04.1) on x86_64-pc-linux-gnu, compiled by gcc (Ubuntu 13.3.0-6ubuntu2~24.04.1) 13.3.0, 64-bit
(1 row)
```

---

## Build Logs, Compiler Release Proof & Resolved Driver Version

### 1. `problem1` Build, Dependency List & Class-File Major Version

```text
$ mvn clean test-compile dependency:list
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------------------< session24:problem1 >-------------------------
[INFO] Building problem1 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:3.3.2:clean (default-clean) @ problem1 ---
[INFO] Deleting /home/user1/Projects/Arbiter/java_24JdbcSqlTransactions/problem1/target
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ problem1 ---
[INFO] skip non existing resourceDirectory /home/user1/Projects/Arbiter/java_24JdbcSqlTransactions/problem1/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ problem1 ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 1 source file with javac [debug release 21] to target/classes
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ problem1 ---
[INFO] skip non existing resourceDirectory /home/user1/Projects/Arbiter/java_24JdbcSqlTransactions/problem1/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ problem1 ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-dependency-plugin:3.6.1:list (default-cli) @ problem1 ---
[INFO] 
[INFO] The following files have been resolved:
[INFO]    org.postgresql:postgresql:jar:42.7.14:compile -- module org.postgresql.jdbc [auto]
[INFO]    org.checkerframework:checker-qual:jar:3.55.1:runtime -- module org.checkerframework.checker.qual
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.765 s
[INFO] Finished at: 2026-11-11T20:27:37+05:30
[INFO] ------------------------------------------------------------------------
```

```text
$ javap -verbose target/classes/session24/problem1/DepartmentEmployeeJoinDemo.class | grep -E "major version|minor version"
  minor version: 0
  major version: 65
```

### 2. `problem2` Build, Dependency List & Class-File Major Version

```text
$ mvn clean test-compile dependency:list
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------------------< session24:problem2 >-------------------------
[INFO] Building problem2 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:3.3.2:clean (default-clean) @ problem2 ---
[INFO] Deleting /home/user1/Projects/Arbiter/java_24JdbcSqlTransactions/problem2/target
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ problem2 ---
[INFO] skip non existing resourceDirectory /home/user1/Projects/Arbiter/java_24JdbcSqlTransactions/problem2/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ problem2 ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 7 source files with javac [debug release 21] to target/classes
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ problem2 ---
[INFO] skip non existing resourceDirectory /home/user1/Projects/Arbiter/java_24JdbcSqlTransactions/problem2/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ problem2 ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-dependency-plugin:3.6.1:list (default-cli) @ problem2 ---
[INFO] 
[INFO] The following files have been resolved:
[INFO]    org.postgresql:postgresql:jar:42.7.14:compile -- module org.postgresql.jdbc [auto]
[INFO]    org.checkerframework:checker-qual:jar:3.55.1:runtime -- module org.checkerframework.checker.qual
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.702 s
[INFO] Finished at: 2026-11-11T20:28:12+05:30
[INFO] ------------------------------------------------------------------------
```

```text
$ javap -verbose target/classes/session24/problem2/TransferDemo.class | grep -E "major version|minor version"
  minor version: 0
  major version: 65
```

---

## Problem 1: Department / Employee Joins

### 1. Schema Execution & `psql \d` Output

```text
$ PGPASSWORD="$ARBITER_DB_PASSWORD" psql "${ARBITER_DB_URL#jdbc:}" -U "$ARBITER_DB_USER" -f Database/problem1_join.sql
psql:Database/problem1_join.sql:1: NOTICE:  table "employee" does not exist, skipping
DROP TABLE
psql:Database/problem1_join.sql:2: NOTICE:  table "department" does not exist, skipping
DROP TABLE
CREATE TABLE
CREATE TABLE
INSERT 0 4
INSERT 0 8
```

```text
$ PGPASSWORD="$ARBITER_DB_PASSWORD" psql "${ARBITER_DB_URL#jdbc:}" -U "$ARBITER_DB_USER" -c "\d department" -c "\d employee"
                       Table "public.department"
 Column |  Type   | Collation | Nullable |           Default            
--------+---------+-----------+----------+------------------------------
 id     | integer |           | not null | generated always as identity
 name   | text    |           | not null | 
Indexes:
    "department_pkey" PRIMARY KEY, btree (id)
    "department_name_key" UNIQUE CONSTRAINT, btree (name)
Referenced by:
    TABLE "employee" CONSTRAINT "employee_department_id_fkey" FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL

                            Table "public.employee"
    Column     |  Type   | Collation | Nullable |           Default            
---------------+---------+-----------+----------+------------------------------
 id            | integer |           | not null | generated always as identity
 full_name     | text    |           | not null | 
 department_id | integer |           |          | 
Indexes:
    "employee_pkey" PRIMARY KEY, btree (id)
Foreign-key constraints:
    "employee_department_id_fkey" FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL
```

### 2. Consecutive Program Runs (Run 1 and Run 2)

#### Run 1

```text
$ mvn -q exec:java
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
=== 1. INNER JOIN (employee -> department, ordered by e.id) ===
  Asha | Engineering
  Bram | Engineering
  Chen | Engineering
  Dara | Sales
  Elif | Sales
  Farid | Support
Row count: 6

=== 2. LEFT JOIN (department on left, ordered by d.id, e.id) ===
  Engineering | Asha
  Engineering | Bram
  Engineering | Chen
  Sales | Dara
  Sales | Elif
  Support | Farid
  Legal | null
Row count: 7

=== 3. LEFT JOIN (employee on left, ordered by e.id) ===
  Asha | Engineering
  Bram | Engineering
  Chen | Engineering
  Dara | Sales
  Elif | Sales
  Farid | Support
  Gita | null
  Hugo | null
Row count: 8

=== 4. GROUP BY department (ordered by d.id) ===
  Engineering | 3
  Sales | 2
  Support | 1
  Legal | 0
Row count: 4
```

#### Run 2

```text
$ mvn -q exec:java
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
=== 1. INNER JOIN (employee -> department, ordered by e.id) ===
  Asha | Engineering
  Bram | Engineering
  Chen | Engineering
  Dara | Sales
  Elif | Sales
  Farid | Support
Row count: 6

=== 2. LEFT JOIN (department on left, ordered by d.id, e.id) ===
  Engineering | Asha
  Engineering | Bram
  Engineering | Chen
  Sales | Dara
  Sales | Elif
  Support | Farid
  Legal | null
Row count: 7

=== 3. LEFT JOIN (employee on left, ordered by e.id) ===
  Asha | Engineering
  Bram | Engineering
  Chen | Engineering
  Dara | Sales
  Elif | Sales
  Farid | Support
  Gita | null
  Hugo | null
Row count: 8

=== 4. GROUP BY department (ordered by d.id) ===
  Engineering | 3
  Sales | 2
  Support | 1
  Legal | 0
Row count: 4
```

### 3. Why Query 4 Gives `0` for `Legal`

In Query 4, `COUNT(e.id)` only increments for rows where `e.id` is non-`NULL`, so even though `Legal` is preserved by the `LEFT JOIN` as one group row with `e.id = NULL`, `COUNT(e.id)` evaluates to `0` instead of `1`.

### 4. Why Filtering the Right Table in `WHERE` Turns a `LEFT JOIN` into an `INNER JOIN`

Take Query 2 from Problem 1 as a concrete example (`department d LEFT JOIN employee e ON d.id = e.department_id`):
- During the join step itself, `Legal` (`d.id = 4`) has no matching rows in `employee`, but because `department` is on the preserved side of `LEFT JOIN`, the join stage still outputs the row `('Legal', NULL)` with `e.id = NULL` and `e.full_name = NULL`.
- Suppose we then add a `WHERE` filter on a column from `employee`, such as `WHERE e.full_name <> 'Asha'`.
- Because `WHERE` is evaluated **after** the join has already produced its candidate rows, it tests `NULL <> 'Asha'` on the `('Legal', NULL)` row. Under SQL's three-valued logic, any regular comparison (`=`, `<>`, `<`, `>`, `LIKE`) involving `NULL` yields `UNKNOWN` rather than `TRUE`, and `WHERE` only passes rows whose predicate is `TRUE`.
- As a result, `('Legal', NULL)` is filtered out by `WHERE`, leaving only departments that had at least one real matching employee row—which is the exact behavior of an `INNER JOIN`.
- Moving that condition into the join predicate (`ON d.id = e.department_id AND e.full_name <> 'Asha'`) restricts which employee rows attach to a department *during* the join while still allowing `LEFT JOIN` to emit `('Legal', NULL)` (and any other department with zero qualifying employees) afterward.

---

## Problem 2: Transactions, Savepoints, Isolation, Batching and Indexes

### Schema Initialization (`Database/problem2_accounts.sql`)

```text
$ PGPASSWORD="$ARBITER_DB_PASSWORD" psql "${ARBITER_DB_URL#jdbc:}" -U "$ARBITER_DB_USER" -f Database/problem2_accounts.sql
psql:Database/problem2_accounts.sql:1: NOTICE:  table "transfer_log" does not exist, skipping
DROP TABLE
psql:Database/problem2_accounts.sql:2: NOTICE:  table "account" does not exist, skipping
DROP TABLE
psql:Database/problem2_accounts.sql:3: NOTICE:  table "sp_demo" does not exist, skipping
DROP TABLE
psql:Database/problem2_accounts.sql:4: NOTICE:  table "batch_item" does not exist, skipping
DROP TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
INSERT 0 3
```

---

### Part (a): `TransferDemo` — Transactions & Aborted-Transaction Rule

#### Program Output

```text
$ mvn -q exec:java -Dexec.mainClass=session24.problem2.TransferDemo
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
=== Step 1: Initial state ===
  Balances: Alice(id=1)=1000.00, Bob(id=2)=100.00, Carol(id=3)=0.00
  Transfer log rows (0): none

=== Step 2: transfer(1, 2, 250.00) ===
  Transfer (1 -> 2, 250.00) SUCCEEDED
  Balances: Alice(id=1)=750.00, Bob(id=2)=350.00, Carol(id=3)=0.00
  Transfer log rows (1): id=1 (1->2: 250.00)

=== Step 3: transfer(2, 1, 400.00) ===
  Transfer (2 -> 1, 400.00) FAILED [INSUFFICIENT_FUNDS]: SQLState=23514, message=Insufficient funds for transfer from 2 to 1
  Balances: Alice(id=1)=750.00, Bob(id=2)=350.00, Carol(id=3)=0.00
  Transfer log rows (1): id=1 (1->2: 250.00)

=== Step 4: transfer(1, 999, 10.00) ===
  Transfer (1 -> 999, 10.00) FAILED [OTHER]: SQLState=null, message=Credit failed: target account id 999 does not exist
  Balances: Alice(id=1)=750.00, Bob(id=2)=350.00, Carol(id=3)=0.00
  Transfer log rows (1): id=1 (1->2: 250.00)

=== Step 5: Aborted-transaction demonstration (23514 -> 25P02 -> rollback) ===
  1) Failing debit caught: SQLState=23514, message=ERROR: new row for relation "account" violates check constraint "account_balance_check"
  Detail: Failing row contains (2, Bob, -50.00).
  2) Follow-up SELECT before rollback caught: SQLState=25P02, message=ERROR: current transaction is aborted, commands ignored until end of transaction block
  3) After rollback, SELECT on same connection succeeded: Bob balance=350.00
  Balances: Alice(id=1)=750.00, Bob(id=2)=350.00, Carol(id=3)=0.00
  Transfer log rows (1): id=1 (1->2: 250.00)

=== Step 6: transfer(1, 3, 50.00) ===
  Transfer (1 -> 3, 50.00) SUCCEEDED
  Balances: Alice(id=1)=700.00, Bob(id=2)=350.00, Carol(id=3)=50.00
  Transfer log rows (2): id=1 (1->2: 250.00), id=2 (1->3: 50.00)
```

#### Justification for `transfer(fromId, toId, amount)` Signature (Opening Its Own Connection)

`transfer(long fromId, long toId, BigDecimal amount)` opens and manages its own `Connection` inside a `try-with-resources` block rather than accepting a caller-supplied `Connection`:
- In `TransferDemo`, `transfer(...)` itself acts as the service-layer transaction boundary that owns the complete business use case (debiting the source account, crediting the destination account, and recording the `transfer_log` entry).
- Owning the `Connection` inside `transfer(...)` guarantees that all six required lifecycle steps (`setAutoCommit(false)`, executing the three statements in order, `commit()`, `rollback()` on any non-commit exit via `if (!committed) conn.rollback()` in `finally`, restoring `setAutoCommit(true)` in an inner `finally`, and closing the connection via `try-with-resources`) are strictly self-contained. Because switching `setAutoCommit(true)` on a JDBC connection implicitly commits an uncommitted transaction, checking `if (!committed) conn.rollback()` inside `finally` before `conn.setAutoCommit(true)` guarantees that even an unchecked `RuntimeException` or `Error` between debit and credit rolls back the debit before auto-commit is restored.

#### Why Step 5's Follow-Up Statement Fails with `25P02` (and Why Session 23 Did Not)

- **Why Step 5 fails with `25P02`:** When `autoCommit` is disabled (`conn.setAutoCommit(false)`), all statements run inside a single explicit PostgreSQL transaction block. As soon as the debit of `400.00` from Bob violates `account_balance_check` (`SQLState=23514`), PostgreSQL marks the entire transaction block as **aborted**. Catching the `SQLException` in Java only handles the Java exception on the client side; the server transaction remains poisoned and rejects every subsequent SQL statement (even a harmless `SELECT count(*) FROM account`) with `SQLState=25P02` (`current transaction is aborted, commands ignored until end of transaction block`) until `conn.rollback()` (or a rollback to a prior savepoint) ends the aborted state.
- **Why Session 23 did not show `25P02`:** In Session 23, the connection remained in JDBC's default `autoCommit = true` mode, where each individual SQL statement runs as its own standalone mini-transaction. When a statement failed with a constraint violation in Session 23, PostgreSQL automatically rolled back and closed that single-statement transaction immediately, leaving the connection in a clean state for the very next statement.

---

### Part (b): `SavepointDemo` — Savepoints & Identity Sequence Gaps

#### Program Output

```text
$ mvn -q exec:java -Dexec.mainClass=session24.problem2.SavepointDemo
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
1. Inserted 'alpha'
2. Set savepoint sp1
3. Inserted 'gamma'
4. Duplicate 'alpha' failed as expected: SQLState=23505, message=ERROR: duplicate key value violates unique constraint "sp_demo_label_key"
  Detail: Key (label)=(alpha) already exists.
5. Rolled back to savepoint sp1
6. Inserted 'beta'
7. Set savepoint sp2, inserted 'delta', released sp2
8. Committed transaction

=== Final rows in sp_demo (ordered by id) ===
  (1, alpha)
  (4, beta)
  (5, delta)
```

#### Why There Are Gaps at IDs 2 and 3, and Why They Are Not Reused

- **How IDs 2 and 3 were consumed:** The `id` column (`GENERATED ALWAYS AS IDENTITY`) is backed by an internal PostgreSQL sequence. Step 1 (`INSERT 'alpha'`) drew `id = 1`. After savepoint `sp1` was created, Step 3 (`INSERT 'gamma'`) drew `id = 2` and inserted the row. In Step 4, attempting to insert the duplicate `'alpha'` evaluated the identity default first—drawing `id = 3` from the sequence—before failing on the unique index check (`sp_demo_label_key`, `SQLState=23505`).
- **Why rolling back to `sp1` does not reuse IDs 2 and 3:** Sequence generators in PostgreSQL advance **non-transactionally** so that concurrent transactions can allocate unique IDs immediately without waiting for other transactions to commit or roll back. When Step 5 executed `conn.rollback(sp1)`, PostgreSQL undid the table modifications made after `sp1` (removing the `'gamma'` tuple with `id = 2` and clearing the `23505` error state), but it never rewinds sequence counters. Consequently, Step 6 (`'beta'`) and Step 7 (`'delta'`) drew the next sequence numbers (`4` and `5`), leaving permanent gaps at `2` and `3`.

---

### Part (c): `IsolationDemo` — Isolation Levels & Anomalies

#### Program Output

```text
$ mvn -q exec:java -Dexec.mainClass=session24.problem2.IsolationDemo
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
=== Isolation Level: READ COMMITTED ===
  Step 1 (A initial read):             Alice=1000.00, 3 rows
  Step 2 (B committed):                Alice +100.00, inserted Dave=10.00
  Step 3 (A second read in same tx):   Alice=1100.00, 4 rows
  Step 4 (A read after commit):        Alice=1100.00, 4 rows

=== Isolation Level: REPEATABLE READ ===
  Step 1 (A initial read):             Alice=1000.00, 3 rows
  Step 2 (B committed):                Alice +100.00, inserted Dave=10.00
  Step 3 (A second read in same tx):   Alice=1000.00, 3 rows
  Step 4 (A read after commit):        Alice=1100.00, 4 rows

=== READ UNCOMMITTED One-Line Experiment ===
  A (SHOW transaction_isolation='read uncommitted') while B holds uncommitted +500.00 on Alice -> A sees Alice=1000.00, 3 rows
```

#### Analysis of Step 3 Anomalies Under `READ COMMITTED` vs `REPEATABLE READ`

- **Anomalies exposed in Step 3 under `READ COMMITTED`:** Because `READ COMMITTED` takes a new snapshot at the start of each statement inside Transaction A, Step 3 sees the changes committed by Connection B in Step 2 and exposes **both**:
  1. **Non-repeatable read (Alice's balance changing from `1000.00` to `1100.00`):** Re-reading the exact same existing row (`WHERE id = 1`) within the same transaction returned a different value because Connection B committed an `UPDATE` to that row in between.
  2. **Phantom read (the row count changing from `3 rows` to `4 rows`):** Re-running `SELECT COUNT(*) FROM account` returned a different count because Connection B committed an `INSERT` of a brand-new row (`Dave`), causing a new row ("phantom") to appear in the set of rows matching the query.
- **Why `REPEATABLE READ` prevented both in Step 3:** Under `REPEATABLE READ`, PostgreSQL freezes Transaction A's snapshot at its very first statement (Step 1), so Step 3 continues to observe `Alice=1000.00, 3 rows` until Transaction A commits and starts a new transaction in Step 4 (`Alice=1100.00, 4 rows`).

#### What `READ UNCOMMITTED` Does in PostgreSQL

Although PostgreSQL accepts `SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED` (and `SHOW transaction_isolation` reports `'read uncommitted'`), PostgreSQL's multiversion concurrency control (MVCC) architecture **never allows dirty reads at any isolation level** and behaves identically to `READ COMMITTED`. In the experiment above, while Connection B held an uncommitted `+500.00` update on Alice, Connection A saw only the last committed state: `Alice=1000.00, 3 rows`.

---

### Part (d): `BatchAndIndexDemo` & `Database/problem2_index.sql`

#### 1. Batch Half — Program Output (`BatchAndIndexDemo`)

```text
$ mvn -f java_24JdbcSqlTransactions/problem2/pom.xml test-compile exec:java -Dexec.mainClass=session24.problem2.BatchAndIndexDemo
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------------------< session24:problem2 >-------------------------
[INFO] Building problem2 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ problem2 ---
[INFO] skip non existing resourceDirectory /home/user1/Projects/Arbiter/java_24JdbcSqlTransactions/problem2/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ problem2 ---
[INFO] Nothing to compile - all classes are up to date.
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ problem2 ---
[INFO] skip non existing resourceDirectory /home/user1/Projects/Arbiter/java_24JdbcSqlTransactions/problem2/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ problem2 ---
[INFO] No sources to compile
[INFO] 
[INFO] --- exec-maven-plugin:3.3.0:java (default-cli) @ problem2 ---
  [1. Row-by-row] elapsed=760 ms | count=20000, sum(qty)=990000, max(id)=20000
=== 2. Batched (standard) ===
  First executeBatch() array length:    1000
  First executeBatch() distinct values: [1]
  [2. Batched (standard)] elapsed=152 ms | count=20000, sum(qty)=990000, max(id)=20000
=== 3. Batched (reWriteBatchedInserts=true) ===
  First executeBatch() array length:    1000
  First executeBatch() distinct values: [-2]
  [3. Batched (reWriteBatchedInserts=true)] elapsed=95 ms | count=20000, sum(qty)=990000, max(id)=20000

=== Timing Summary ===
  Row-by-row elapsed:                      760 ms
  Batched elapsed:                         152 ms
  Batched (reWriteBatchedInserts) elapsed: 95 ms
  Ratio (row-by-row / batched):            5.00x
  Ratio (row-by-row / batched-rewrite):    8.00x
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.173 s
[INFO] Finished at: 2026-11-11T20:44:46+05:30
[INFO] ------------------------------------------------------------------------
```

#### Explanation of `executeBatch()` Return Array & Why Batching Reduces Elapsed Time

- **`executeBatch()` return array observations:**
  - In **Variant 2 (Standard Batch)**, the first `executeBatch()` returns an array of length `1000` with distinct value `[1]`, reporting the affected row count (`1`) for each of the 1,000 queued parameter sets.
  - In **Variant 3 (`reWriteBatchedInserts=true`)**, the first `executeBatch()` still returns an array of length `1000` (one entry per `addBatch()` call), but its distinct value is `[-2]` (`Statement.SUCCESS_NO_INFO`). Because `reWriteBatchedInserts=true` allows the driver to combine individual single-row `INSERT`s into multi-row `INSERT INTO batch_item (code, qty) VALUES (...), (...), ...` statements, per-element row counts are not tracked individually and the driver reports `Statement.SUCCESS_NO_INFO` (`-2`).
- **Execution calls and network turnaround:**
  - **Row-by-row (`760 ms`):** Calls `ps.executeUpdate()` 20,000 times inside the loop. Every `executeUpdate()` is a synchronous request/response cycle that sends a single row's parameters and blocks until the database responds, so the loop waits on **20,000 individual statement round trips** (plus `commit()`). Because inserting a small two-column row on the server is very fast, waiting on 20,000 back-and-forth socket turnarounds dominates total elapsed time.
  - **Standard Batched (`152 ms`, `5.00x` faster):** Calls `addBatch()` to queue parameter sets in client memory with no network traffic and calls `executeBatch()` only **20 times** (once per 1,000 rows), avoiding 20,000 separate synchronous `executeUpdate()` waits.
  - **Batched with `reWriteBatchedInserts=true` (`95 ms`, `8.00x` faster):** Also flushes 20 batches of 1,000 rows, and additionally folds the queued single-row `INSERT`s into fewer multi-row `INSERT ... VALUES (...), (...)` statements. *(Note: exact low-level wire packet counts under driver pipelining and internal server CPU breakdowns were not measured directly.)*

---

#### 2. Index Half — `psql` Output (`Database/problem2_index.sql`)

```text
$ PGPASSWORD="$ARBITER_DB_PASSWORD" psql "${ARBITER_DB_URL#jdbc:}" -U "$ARBITER_DB_USER" -e -f ../../Database/problem2_index.sql
DROP TABLE IF EXISTS customer_big;
DROP TABLE
CREATE TABLE customer_big (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email TEXT NOT NULL,
    country TEXT NOT NULL
);
CREATE TABLE
INSERT INTO customer_big (email, country)
SELECT
    'User' || g || '@Example.com',
    (ARRAY['IN', 'US', 'DE', 'FR', 'JP'])[((g - 1) % 5) + 1]
FROM generate_series(1, 300000) AS g;
INSERT 0 300000
ANALYZE customer_big;
ANALYZE
EXPLAIN (ANALYZE)
SELECT * FROM customer_big WHERE email = 'User123456@Example.com';
                                                       QUERY PLAN                                                        
-------------------------------------------------------------------------------------------------------------------------
 Gather  (cost=1000.00..5411.98 rows=1 width=29) (actual time=3.991..12.015 rows=1 loops=1)
   Workers Planned: 1
   Workers Launched: 1
   ->  Parallel Seq Scan on customer_big  (cost=0.00..4411.88 rows=1 width=29) (actual time=5.319..7.757 rows=0 loops=2)
         Filter: (email = 'User123456@Example.com'::text)
         Rows Removed by Filter: 150000
 Planning Time: 0.085 ms
 Execution Time: 12.028 ms
(8 rows)

CREATE INDEX idx_customer_big_email ON customer_big (email);
CREATE INDEX
ANALYZE customer_big;
ANALYZE
EXPLAIN (ANALYZE)
SELECT * FROM customer_big WHERE email = 'User123456@Example.com';
                                                              QUERY PLAN                                                              
--------------------------------------------------------------------------------------------------------------------------------------
 Index Scan using idx_customer_big_email on customer_big  (cost=0.42..8.44 rows=1 width=29) (actual time=0.026..0.026 rows=1 loops=1)
   Index Cond: (email = 'User123456@Example.com'::text)
 Planning Time: 0.137 ms
 Execution Time: 0.037 ms
(4 rows)

EXPLAIN (ANALYZE)
SELECT * FROM customer_big WHERE lower(email) = 'user123456@example.com';
                                                         QUERY PLAN                                                          
-----------------------------------------------------------------------------------------------------------------------------
 Gather  (cost=1000.00..6003.06 rows=1500 width=29) (actual time=42.777..45.328 rows=1 loops=1)
   Workers Planned: 1
   Workers Launched: 1
   ->  Parallel Seq Scan on customer_big  (cost=0.00..4853.06 rows=882 width=29) (actual time=29.687..41.725 rows=0 loops=2)
         Filter: (lower(email) = 'user123456@example.com'::text)
         Rows Removed by Filter: 150000
 Planning Time: 0.029 ms
 Execution Time: 45.343 ms
(8 rows)

CREATE INDEX idx_customer_big_lower_email ON customer_big (lower(email));
CREATE INDEX
ANALYZE customer_big;
ANALYZE
EXPLAIN (ANALYZE)
SELECT * FROM customer_big WHERE lower(email) = 'user123456@example.com';
                                                                 QUERY PLAN                                                                 
--------------------------------------------------------------------------------------------------------------------------------------------
 Index Scan using idx_customer_big_lower_email on customer_big  (cost=0.42..8.44 rows=1 width=29) (actual time=0.024..0.024 rows=1 loops=1)
   Index Cond: (lower(email) = 'user123456@example.com'::text)
 Planning Time: 0.106 ms
 Execution Time: 0.034 ms
(4 rows)
```

#### General Reasons the Planner May Ignore an Index

PostgreSQL's query planner is cost-based: it uses table statistics to estimate the total cost of each candidate execution plan and selects the cheapest one. The primary reasons an index is not used (or a plain `Index Scan` is not chosen) are:

1. **Small tables:** If a table occupies only a handful of disk pages, reading the table sequentially in a single pass (`Seq Scan`) costs less than traversing index pages and then fetching table pages.
2. **Predicate matches a large portion of the table (low selectivity):** Each row fetched via a plain `Index Scan` requires a random access into the table heap. When a query matches a large fraction of rows, the cumulative cost of random heap fetches exceeds the cost of reading the table sequentially (`Seq Scan`), or for intermediate result sizes the planner may use a `Bitmap Index Scan` + `Bitmap Heap Scan` to sort matching page locations before reading the heap.
3. **Stale statistics:** Because the planner relies on statistics gathered by `ANALYZE` to estimate how many rows match a predicate, outdated statistics (such as right after a bulk load or after creating an expression index without running `ANALYZE`) can cause the planner to misestimate row counts and pick a suboptimal plan.
4. **Function-wrapped columns:** A standard B-tree index on `(col)` stores and orders the raw `col` values, not the result of `func(col)`. A query filtering on `WHERE lower(email) = 'user123456@example.com'` cannot use a plain index on `(email)` because the index does not contain `lower(email)` keys; the planner must scan every row and compute `lower(email)` unless an **expression index** on `(lower(email))` is created.

#### Analysis of Plan-Node Family Transitions and Estimated vs. Actual Rows

| Step | Query / Index State | Plan-Node Family | Estimated `rows=` | Actual `rows=` | Execution Time |
|---|---|---|---|---|---|
| 1 | `WHERE email = 'User123456@Example.com'` (no index on `email`) | **Seq Scan** (`Gather` -> `Parallel Seq Scan`) | `1` | `1` | `12.028 ms` |
| 2 | `WHERE email = 'User123456@Example.com'` (with `idx_customer_big_email`) | **Index Scan** (`idx_customer_big_email`) | `1` | `1` | `0.037 ms` |
| 3 | `WHERE lower(email) = 'user123456@example.com'` (only plain `idx_customer_big_email`) | **Seq Scan** (`Gather` -> `Parallel Seq Scan`) | `1500` (`882` per worker) | `1` | `45.343 ms` |
| 4 | `WHERE lower(email) = 'user123456@example.com'` (with expression index `idx_customer_big_lower_email`) | **Index Scan** (`idx_customer_big_lower_email`) | `1` | `1` | `0.034 ms` |

1. **Step 1 -> Step 2 (`Seq Scan` to `Index Scan`):**
   - Without an index on `email`, PostgreSQL has no sorted lookup structure for `email` and must scan the 300,000-row table (`Parallel Seq Scan` under `Gather`, filtering out 150,000 rows per worker across 2 loops). Because `ANALYZE customer_big` ran after the bulk load, the planner knew `email` values are unique and estimated `rows=1` (matching actual `rows=1`).
   - Creating `idx_customer_big_email ON customer_big (email)` and running `ANALYZE customer_big` switched the plan to an **Index Scan** (`cost=0.42..8.44`), walking the B-tree in $O(\log n)$ time and fetching the single matching row in `0.037 ms` (`rows=1` estimated, `rows=1` actual).
2. **Step 2 -> Step 3 (`Index Scan` back to `Seq Scan` when wrapping column in `lower(email)`):**
   - Even with `idx_customer_big_email` present, `WHERE lower(email) = 'user123456@example.com'` cannot use the plain index because `idx_customer_big_email` is ordered by the raw mixed-case `email` strings (`'User123456@Example.com'`), not by `lower(email)`. PostgreSQL therefore falls back to a **Parallel Seq Scan** (`45.343 ms`), evaluating `lower(email)` across all 300,000 rows.
   - Comparing **estimated vs. actual rows** in Step 3: without an expression index, PostgreSQL has no statistics on the expression `lower(email)`, so the planner guesses `rows=1500` at the `Gather` node (`rows=882` per parallel worker) whereas actual rows returned is `1`.
3. **Step 3 -> Step 4 (`Seq Scan` to `Index Scan` via Expression Index):**
   - Creating `idx_customer_big_lower_email ON customer_big (lower(email))` stores the precomputed `lower(email)` keys in the index, matching `WHERE lower(email) = 'user123456@example.com'`. Because stored emails in `customer_big` are mixed-case (`'User123456@Example.com'`), rewriting the query to compare the bare `email` column is not logically equivalent to a case-insensitive lookup; the expression index is the verified fix.
   - Running `ANALYZE customer_big` after creating `idx_customer_big_lower_email` also collected statistics on `lower(email)`, bringing the planner's estimate from `rows=1500` back to `rows=1` (matching actual `rows=1`) and switching the plan back to an **Index Scan** (`0.034 ms`).
