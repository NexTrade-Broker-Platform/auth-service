CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (
    id,
    email,
    password_hash,
    username,
    first_name,
    last_name,
    date_of_birth,
    created_at,
    is_active,
    is_admin
)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'admin@nextrade.com',
    crypt('Admin123!', gen_salt('bf', 10)),
    'admin',
    'Platform',
    'Admin',
    '1990-01-01',
    NOW(),
    true,
    true
);
