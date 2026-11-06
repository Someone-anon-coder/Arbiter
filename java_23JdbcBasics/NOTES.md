# Session 23: JDBC Basics - Execution Notes

## 1. System Versions

**PostgreSQL Version:**
```text
psql -h 127.0.0.1 -U $ARBITER_DB_USER -d arbiter_db -c "SELECT version();"
PostgreSQL 16.15 (Ubuntu 16.15-0ubuntu0.24.04.1) on x86_64-pc-linux-gnu, compiled by gcc (Ubuntu 13.3.0-6ubuntu2~24.04.1) 13.3.0, 64-bit

```

**Resolved PostgreSQL Driver Version:**

```text
mvn dependency:list | grep org.postgresql
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
[INFO]    org.postgresql:postgresql:jar:42.7.14:compile

```

**Java Class File Major Version Proof (Java 21):**

```text
javap -v target/classes/problem1/Problem1.class | grep major
  major version: 65

```

---

## 2. Database Schema

**`\d lab_item` Output:**

```text
                                Table "public.lab_item"
  Column  |         Type          | Collation | Nullable |           Default            
----------+-----------------------+-----------+----------+------------------------------
 id       | integer               |           | not null | generated always as identity
 name     | character varying(40) |           | not null | 
 quantity | integer               |           | not null | 
 status   | character varying(10) |           | not null | 'NEW'::character varying
Indexes:
    "lab_item_pkey" PRIMARY KEY, btree (id)
    "lab_item_name_key" UNIQUE CONSTRAINT, btree (name)
Check constraints:
    "lab_item_quantity_check" CHECK (quantity >= 0)
    "lab_item_status_check" CHECK (status::text = ANY (ARRAY['NEW'::character varying, 'USED'::character varying, 'BROKEN'::character varying]::text[]))

```

**`\d member` Output:**

```text
                                     Table "public.member"
     Column     |          Type          | Collation | Nullable |           Default            
----------------+------------------------+-----------+----------+------------------------------
 id             | bigint                 |           | not null | generated always as identity
 email          | character varying(100) |           | not null | 
 full_name      | character varying(60)  |           | not null | 
 age            | integer                |           | not null | 
 phone          | character varying(20)  |           |          | 
 loyalty_points | integer                |           |          | 
 joined_on      | date                   |           | not null | 
Indexes:
    "member_pkey" PRIMARY KEY, btree (id)
    "member_email_key" UNIQUE CONSTRAINT, btree (email)
Check constraints:
    "member_age_chk" CHECK (age >= 13 AND age <= 120)

```

---

## 3. Exceptions and Integrity Failures (Problem 2)

**Duplicate Key Failure:**

* **Category:** `DUPLICATE_KEY`
* **SQLState:** `23505`
* **Raw Message:**
```text
ERROR: duplicate key value violates unique constraint "member_email_key"
  Detail: Key (email)=(ada@example.com) already exists.

```



**CHECK Constraint Failure (Age 12):**

* **Category:** `CONSTRAINT_VIOLATION`
* **SQLState:** `23514`
* **Raw Message:**
```text
ERROR: new row for relation "member" violates check constraint "member_age_chk"
  Detail: Failing row contains (5, kid@example.com, Kid, 12, null, 0, 2025-01-01).

```



**NOT NULL Constraint Failure (Null Name):**

* **Category:** `CONSTRAINT_VIOLATION`
* **SQLState:** `23502`
* **Raw Message:**
```text
ERROR: null value in column "full_name" of relation "member" violates not-null constraint
  Detail: Failing row contains (6, noname@example.com, null, 30, null, 0, 2025-01-01).

```



### Note on Alan's Sequence ID

Alan receives an ID of `7` rather than `4` because PostgreSQL sequences (which power `GENERATED ALWAYS AS IDENTITY`) increment independently of transactions. When the three failing `INSERT` statements were attempted, the database generated and consumed IDs 4, 5, and 6 before the constraints rejected the rows. Those IDs are permanently burned, leaving 7 as the next available ID for Alan.

---

## 4. Why PreparedStatement Prevents SQL Injection

When you build a query using string concatenation, the database receives a single, combined block of text. It has to parse that text to figure out what is a command and what is just data. If a user inputs a single quote, they can trick the parser into ending the string early and reading the rest of their input as executable SQL commands.

A `PreparedStatement` completely eliminates this risk by separating the query structure from the user data. The database receives and parses the SQL structure first, locking in the syntax tree. When the user's data is sent over afterwards during the bind phase, the database already knows it is strictly a literal value. Even if the data contains quotes, semicolons, or SQL keywords, it is treated purely as data—meaning the user's input can never alter the structure of the commands being executed.

---

## 5. Execution Proof

### Problem 1 Execution

**Run 1:**

```text
mvn exec:java -Dexec.mainClass="problem1.Problem1" -q
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
1 | Oscilloscope | 3 | NEW
2 | Soldering Iron | 12 | USED
3 | Multimeter | 7 | BROKEN

```

**Run 2 (Identical output proves TRUNCATE is working):**

```text
mvn exec:java -Dexec.mainClass="problem1.Problem1" -q
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
1 | Oscilloscope | 3 | NEW
2 | Soldering Iron | 12 | USED
3 | Multimeter | 7 | BROKEN

```

### Problem 2 Execution

**Run 1:**

```text
mvn exec:java -Dexec.mainClass="problem2.Problem2" -q
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
Created initial IDs: 1, 2, 3
Failure [Duplicate]: Category: DUPLICATE_KEY, SQLState: 23505, Raw message: ERROR: duplicate key value violates unique constraint "member_email_key"
  Detail: Key (email)=(ada@example.com) already exists.
Failure [Age 12]: Category: CONSTRAINT_VIOLATION, SQLState: 23514, Raw message: ERROR: new row for relation "member" violates check constraint "member_age_chk"
  Detail: Failing row contains (5, kid@example.com, Kid, 12, null, 0, 2025-01-01).
Failure [Null Name]: Category: CONSTRAINT_VIOLATION, SQLState: 23502, Raw message: ERROR: null value in column "full_name" of relation "member" violates not-null constraint
  Detail: Failing row contains (6, noname@example.com, null, 30, null, 0, 2025-01-01).
Alan ID: 7
findById(2): Optional[Member[id=2, email=grace@example.com, fullName=Grace Hopper, age=45, phone=555-0102, loyaltyPoints=null, joinedOn=2023-12-31]]
findById(3): Optional[Member[id=3, email=linus@example.com, fullName=Linus T, age=28, phone=null, loyaltyPoints=0, joinedOn=2025-06-15]]
findById(999): Optional.empty
findByEmail(linus): Optional[Member[id=3, email=linus@example.com, fullName=Linus T, age=28, phone=null, loyaltyPoints=0, joinedOn=2025-06-15]]
Update counts: 1, 1, 0
delete(3) 1: true
delete(3) 2: false
findById(3) after delete: Optional.empty
Injection Demo: vulnerable method 3 rows, safe method 0 rows
--- FINAL REPORT ---
id 1 has 120 points, phone none, 2024-02-29, 672 days
id 2 has none points, phone 555-0102, 2023-12-31, 732 days
id 7 has 90 points, phone 555-0104, 2025-01-01, 365 days
Aggregates: 3 members, total points 210, average 105.0, 1 member(s) with NULL points, 1 with NULL phone

```

**Run 2 (Identical output proves TRUNCATE and deterministic generation):**

```text
mvn exec:java -Dexec.mainClass="problem2.Problem2" -q
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/usr/share/maven/lib/guava.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
Created initial IDs: 1, 2, 3
Failure [Duplicate]: Category: DUPLICATE_KEY, SQLState: 23505, Raw message: ERROR: duplicate key value violates unique constraint "member_email_key"
  Detail: Key (email)=(ada@example.com) already exists.
Failure [Age 12]: Category: CONSTRAINT_VIOLATION, SQLState: 23514, Raw message: ERROR: new row for relation "member" violates check constraint "member_age_chk"
  Detail: Failing row contains (5, kid@example.com, Kid, 12, null, 0, 2025-01-01).
Failure [Null Name]: Category: CONSTRAINT_VIOLATION, SQLState: 23502, Raw message: ERROR: null value in column "full_name" of relation "member" violates not-null constraint
  Detail: Failing row contains (6, noname@example.com, null, 30, null, 0, 2025-01-01).
Alan ID: 7
findById(2): Optional[Member[id=2, email=grace@example.com, fullName=Grace Hopper, age=45, phone=555-0102, loyaltyPoints=null, joinedOn=2023-12-31]]
findById(3): Optional[Member[id=3, email=linus@example.com, fullName=Linus T, age=28, phone=null, loyaltyPoints=0, joinedOn=2025-06-15]]
findById(999): Optional.empty
findByEmail(linus): Optional[Member[id=3, email=linus@example.com, fullName=Linus T, age=28, phone=null, loyaltyPoints=0, joinedOn=2025-06-15]]
Update counts: 1, 1, 0
delete(3) 1: true
delete(3) 2: false
findById(3) after delete: Optional.empty
Injection Demo: vulnerable method 3 rows, safe method 0 rows
--- FINAL REPORT ---
id 1 has 120 points, phone none, 2024-02-29, 672 days
id 2 has none points, phone 555-0102, 2023-12-31, 732 days
id 7 has 90 points, phone 555-0104, 2025-01-01, 365 days
Aggregates: 3 members, total points 210, average 105.0, 1 member(s) with NULL points, 1 with NULL phone

```

If an exception is thrown in the body of the try block, execution immediately halts, and the AutoCloseable resources are closed in the exact reverse order of their declaration (first ResultSet, then PreparedStatement, then Connection). This guarantees the Connection is closed and returned to the system before the catch block even begins executing.

If the close() method itself throws an exception during this cleanup process, Java handles it via exception suppression. If the try body already threw a primary exception, the new exception from close() is attached to that primary exception via Throwable.addSuppressed(), ensuring the original root cause is not overwritten or lost.
