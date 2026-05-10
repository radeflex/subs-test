--liquibase formatted sql

--changeset radeflex:1
CREATE TABLE IF NOT EXISTS users(
    id SERIAL PRIMARY KEY,
    username VARCHAR(63) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS product(
    id SERIAL PRIMARY KEY,
    name VARCHAR(63) NOT NULL UNIQUE,
    price INT NOT NULL
);

CREATE TABLE IF NOT EXISTS subscription(
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users ON DELETE CASCADE,
    product_id INT REFERENCES product ON DELETE CASCADE,
    status VARCHAR(63) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    paused_at TIMESTAMP
)