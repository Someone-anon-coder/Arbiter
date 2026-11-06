# java_23JdbcBasics — JDBC basics: connections, PreparedStatement, ResultSet, and CRUD

## API vs driver
`java.sql` is almost all interfaces (`Connection`, `PreparedStatement`, `ResultSet`, `SQLException`). `org.postgresql:postgresql` 42.7.14 implements them and registers itself through `META-INF/services/java.sql.Driver`, so `DriverManager` finds it with no `Class.forName`. The URL (`jdbc:postgresql://host:port/db`) picks the driver; nothing in the DAO imports an `org.postgresql` type. Settings come from `ARBITER_DB_URL`, `ARBITER_DB_USER` and `ARBITER_DB_PASSWORD`, so no credential is ever in a committed file.

## A Connection is a network session
Opening one means a TCP connect, authentication, and (on PostgreSQL) a dedicated server-side backend process, counted against `max_connections`. That is why connections are expensive and why leaks matter. It is also the motivation for pooling (Session 25). Both solutions open one connection in a try-with-resources header and share it.

## Why a placeholder prevents injection
Concatenation gives the server one string to parse, so a quote in the input can close a string literal and the rest becomes SQL syntax. With `?` the statement text is parsed first; values arrive afterwards in a separate bind step as typed data, so the syntax tree is already fixed. The demo input `' OR '1'='1` made the concatenated query read `WHERE email = '' OR '1'='1'`, which is true for every row: **3 rows** returned, against **0** from the `PreparedStatement`, which looked for an email literally equal to that text.

## Resource closing
Resources declared in a try-with-resources header close in reverse order (`ResultSet`, then `PreparedStatement`, then `Connection`), and they close **before** any `catch` runs. An exception from `close()` is attached with `addSuppressed` instead of replacing the primary one. In the DAO the `Connection` is owned by `main`, not the DAO, so a failing statement closes only its own `PreparedStatement`; the connection stays open. In auto-commit mode (the default) a failed statement does not affect the next one, which is why Alan's insert succeeds right after three failures. Inside an explicit transaction PostgreSQL behaves differently (Session 24).

## NULL, the cursor, and dates
A `ResultSet` starts **before** the first row; a getter before the first `next()` or after it returns `false` throws `SQLException` (SQLState `24000`). `getInt` returns `0` for NULL, so `0` and NULL are indistinguishable unless `wasNull()` is called straight after the read. The DAO instead uses `getObject("loyalty_points", Integer.class)`, which returns `null` for NULL. In the run, Grace (NULL) prints `none` and Linus (stored `0`) prints `0`, and the average is 105.0 because the NULL is excluded from both sum and count (coercing it to 0 would give 70.0). On the way in, `setInt` cannot take a null `Integer`, so the DAO branches to `setNull(i, Types.INTEGER)`. `DATE` round-trips as `LocalDate` through `setObject` / `getObject(col, LocalDate.class)`; 2024-02-29 to 2026-01-01 is 672 days.

## Failures and SQLState
Classification uses the five-character SQLState, never message text; the driver's `getErrorCode()` is `0` for PostgreSQL, so it is useless here. Class `23` is integrity-constraint violation:

| Failure | SQLState | App category |
|---|---|---|
| UNIQUE (duplicate `email`) | `23505` | `DUPLICATE_KEY` |
| CHECK (`member_age_chk`, age 12) | `23514` | `CONSTRAINT_VIOLATION` |
| NOT NULL (`full_name`) | `23502` | `CONSTRAINT_VIOLATION` |

The raw driver messages carry table names, constraint names and even a `Failing row contains (...)` line with the row's data, so `MemberDataException` exposes only a fixed safe message plus a category, and keeps the `SQLException` as the cause for logs. Every failed insert still consumed an identity value (ids 4, 5, 6 burned), so Alan got **7**, not 4: sequences are not transactional. The schema, not Java, rejects these rows; the same errors appear when inserting from `psql` directly.

## JDBC methods used
| Method | Returns | Used for |
|---|---|---|
| `DriverManager.getConnection(url, user, pw)` | `Connection` | Opens the network session |
| `connection.prepareStatement(sql)` | `PreparedStatement` | Parses `sql` with `?` placeholders |
| `connection.createStatement()` | `Statement` | Only in the labeled anti-example |
| `ps.setString` / `setInt` / `setLong` (1-indexed) | `void` | Binds a value |
| `ps.setNull(i, Types.X)` | `void` | Binds SQL NULL explicitly |
| `ps.setObject(i, localDate)` | `void` | Binds a `LocalDate` to a `DATE` column |
| `ps.executeQuery()` | `ResultSet` | `SELECT`, and `INSERT ... RETURNING id` |
| `ps.executeUpdate()` | `int` rows affected | `INSERT` / `UPDATE` / `DELETE` / `TRUNCATE` (0 for DDL) |
| `statement.execute(sql)` | `boolean` (`true` = a ResultSet exists) | Not used; for unknown or mixed statements |
| `rs.next()` | `boolean` | Advances the cursor; `false` = no more rows |
| `rs.getString` / `getInt` / `getLong` | value (`getInt` gives `0` for NULL) | Reads by column name or 1-based index |
| `rs.getObject(col, Integer.class)` | `Integer`, `null` for NULL | NULL-faithful read |
| `rs.getObject(col, LocalDate.class)` | `LocalDate` | `DATE` as java.time |
| `rs.wasNull()` | `boolean` | Whether the last column read was NULL |
| `sqlException.getSQLState()` | 5-char `String` | Portable failure classification |

## Key takeaway
Integrity belongs in the database (it protects every writer and settles races), and data belongs in bind parameters, never in SQL text. A DAO's job is then to map rows faithfully (NULL stays NULL), close what it opens, and turn `SQLException` into a small domain exception callers can act on without knowing any SQL.
