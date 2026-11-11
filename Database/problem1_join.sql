DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE employee (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name TEXT NOT NULL,
    department_id INTEGER REFERENCES department(id) ON DELETE SET NULL
);

INSERT INTO department (name) VALUES
    ('Engineering'),
    ('Sales'),
    ('Support'),
    ('Legal');

INSERT INTO employee (full_name, department_id) VALUES
    ('Asha', 1),
    ('Bram', 1),
    ('Chen', 1),
    ('Dara', 2),
    ('Elif', 2),
    ('Farid', 3),
    ('Gita', NULL),
    ('Hugo', NULL);