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
           '550e8400-e29b-41d4-a716-446655440000',
           'user01@gmail.com',
           crypt('Parola123', gen_salt('bf', 10)),
           'h4ck3r',
           'Cristi',
           'Popescu',
           '2000-10-10',
           '2026-04-15 12:12:12',
           true,
           false
       );