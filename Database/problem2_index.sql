DROP TABLE IF EXISTS customer_big;

CREATE TABLE customer_big (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email TEXT NOT NULL,
    country TEXT NOT NULL
);

INSERT INTO customer_big (email, country)
SELECT
    'User' || g || '@Example.com',
    (ARRAY['IN', 'US', 'DE', 'FR', 'JP'])[((g - 1) % 5) + 1]
FROM generate_series(1, 300000) AS g;

ANALYZE customer_big;

-- 1. Before any index on email
EXPLAIN (ANALYZE)
SELECT * FROM customer_big WHERE email = 'User123456@Example.com';

-- 2. Plain B-tree index on email
CREATE INDEX idx_customer_big_email ON customer_big (email);
ANALYZE customer_big;

EXPLAIN (ANALYZE)
SELECT * FROM customer_big WHERE email = 'User123456@Example.com';

-- 3. Case-insensitive predicate wrapping indexed column with lower(email)
EXPLAIN (ANALYZE)
SELECT * FROM customer_big WHERE lower(email) = 'user123456@example.com';

-- 4. Expression index on lower(email)
CREATE INDEX idx_customer_big_lower_email ON customer_big (lower(email));
ANALYZE customer_big;

EXPLAIN (ANALYZE)
SELECT * FROM customer_big WHERE lower(email) = 'user123456@example.com';