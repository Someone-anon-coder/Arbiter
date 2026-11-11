# java_24JdbcSqlTransactions — SQL joins and indexes, JDBC batching, JDBC transactions

## Joins, and the LEFT JOIN trap
`ON` decides which rows pair up; `WHERE` runs afterwards and discards result rows. A `LEFT JOIN` keeps every left row and NULL-extends the right columns when nothing matches. A NULL never equals anything, so a `WHERE` test on a right-hand column (`r.col = 5`, `<> 'x'`, `LIKE`) evaluates to NULL for those NULL-extended rows and `WHERE` drops them: the query silently behaves like an `INNER JOIN`. To filter the right table and keep unmatched left rows, put the condition in `ON`. `r.col IS NULL` is the one right-column test that keeps them on purpose (the anti-join idiom).

After a LEFT JOIN, an unmatched group still holds one NULL-extended row. `COUNT(*)` counts it (1); `COUNT(right_table.id)` skips the NULL (0). Count a right-table column that is NULL exactly when there was no match.

## Indexes and the planner
A B-tree index is a sorted tree of keys pointing at rows: lookup is O(log n) instead of an O(n) scan (Session 19's Big-O), paid for with disk space and extra work on every write. A `UNIQUE` or `PRIMARY KEY` constraint is enforced by a unique index. In a composite index `(a, b)` only a leftmost prefix (`a`, or `a` and `b`) can drive a lookup.

The planner is cost-based and uses table statistics (`ANALYZE` refreshes them; run it after a bulk load and after creating an index). Verified on PostgreSQL 16.15 with 300,000 rows: a single-row lookup was a `Gather` over `Parallel Seq Scan` without an index and an `Index Scan` with one. A function on the column (`lower(email)`) cannot use a plain index on `email`, because the index stores `email` values, not `lower(email)` values; it needs an expression index on `lower(email)`. An index can also go unused when the table is tiny or the predicate matches a large share of rows, but the crossover is cost-based, not a fixed percentage: at 20% per value the planner still chose a Bitmap Index Scan here.

## Batching
Each `executeUpdate()` is one network round trip, and for small inserts the round trip dominates the cost. `addBatch()` queues parameter sets client-side; `executeBatch()` sends them together and returns one `int` per element. Flush in chunks to bound memory, and run the whole batch in one transaction. Observed: the standard batch returned `1` for every element; with `reWriteBatchedInserts=true` it returned `-2` (`SUCCESS_NO_INFO`) for every element, so never assume `1`. Timings are reported, not asserted.

## Transactions
Auto-commit makes every statement its own transaction. `setAutoCommit(false)` opens an explicit one: nothing is permanent until `commit()`, and `rollback()` discards everything since the last commit. The pattern is `try-with-resources` for the `Connection`, `setAutoCommit(false)`, work, `commit()`, a rollback on every non-commit exit, and `setAutoCommit(true)` in a `finally` after the rollback. Verified by mutation: removing the rollback call let `setAutoCommit(true)` commit the half-finished transfer.

## The aborted-transaction rule
In PostgreSQL, once a statement fails inside an open transaction the whole transaction is aborted. Every later statement fails with SQLState `25P02` ("current transaction is aborted, commands ignored until end of transaction block") until `rollback()` (or a rollback to a savepoint taken before the failure). Catching the `SQLException` in Java does not rescue it. In auto-commit mode each statement is its own transaction, so a failure aborts only itself, which is why Session 23's shared connection stayed usable. The reason for the rule is atomicity: a transaction is all-or-nothing, so after one statement has failed the server will not let the rest be committed as if nothing happened.

## Savepoints and sequences
A savepoint lets you undo part of a transaction. `rollback(sp)` undoes everything after `sp`, including later statements that succeeded, and the transaction becomes usable again. `releaseSavepoint` only forgets the marker and undoes nothing. Identity values are drawn from a sequence that never rewinds, so rolled-back and failed inserts leave permanent gaps (the demo's ids `1, 4, 5`).

## Isolation levels in PostgreSQL
| Level | Dirty read | Non-repeatable read | Phantom | Notes |
|---|---|---|---|---|
| READ UNCOMMITTED | No | Yes | Yes | Accepted, but behaves as READ COMMITTED; PostgreSQL has no dirty reads |
| READ COMMITTED (default) | No | Yes | Yes | Each statement sees a fresh snapshot |
| REPEATABLE READ | No | No | No (in PostgreSQL) | One snapshot per transaction; a conflicting concurrent update fails with `40001` |
| SERIALIZABLE | No | No | No | Also detects serialization anomalies; aborts with `40001`, which is retryable |

The demo confirmed the READ COMMITTED and REPEATABLE READ rows (second read `1100.00` / 4 rows versus `1000.00` / 3 rows) and that READ UNCOMMITTED showed no uncommitted data. The READ UNCOMMITTED row shows the behaviour documented for PostgreSQL (the same as READ COMMITTED); the demo only checked the no-dirty-read part. Deadlocks (SQLState `40P01`) are a lock cycle that the database breaks by aborting one transaction, which should be retried.

## Transaction control methods
| Method | What it does |
|---|---|
| `setAutoCommit(false)` | Starts an explicit transaction; `true` restores per-statement commit (and commits anything still open) |
| `commit()` | Makes all work since the last commit permanent |
| `rollback()` | Discards all work since the last commit; ends an aborted transaction |
| `setSavepoint()` / `setSavepoint(name)` | Marks a point inside the current transaction |
| `rollback(Savepoint)` | Undoes work after the savepoint; the transaction stays open and usable |
| `releaseSavepoint(Savepoint)` | Forgets the savepoint; nothing is undone |
| `setTransactionIsolation(level)` | Sets the isolation level; call it before the first statement of the transaction |

## Where the boundary belongs
A DAO method does one small thing; a use case such as "transfer money" needs several to succeed or fail together. If each DAO method opened its own connection and committed, a failed credit could not undo the debit. The transaction boundary belongs in the service layer, which owns the whole use case. Category 21's `@Transactional` automates exactly this.

## Key takeaway
A transaction is a unit of atomic work with a visibility rule. Decide the boundary in the service layer, always roll back on failure, and remember that in PostgreSQL a failure inside a transaction poisons it until you roll back.
