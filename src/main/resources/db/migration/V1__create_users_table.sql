CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     password_hash VARCHAR(255) NOT NULL,
                                     username VARCHAR(255) NOT NULL UNIQUE,
                                     first_name VARCHAR(255) NOT NULL,
                                     last_name VARCHAR(255) NOT NULL,
                                     date_of_birth DATE NOT NULL,
                                     created_at TIMESTAMP NOT NULL,
                                     is_active BOOLEAN NOT NULL DEFAULT true,
                                     is_admin BOOLEAN NOT NULL DEFAULT false
);