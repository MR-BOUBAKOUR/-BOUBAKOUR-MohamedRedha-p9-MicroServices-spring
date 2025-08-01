CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    url_picture TEXT,
    locale VARCHAR(10),
    role VARCHAR(50) NOT NULL
    );

INSERT INTO users (username, email, password, url_picture, locale, role)
VALUES (
    'prenom_medecin',
    'medecin.medilabosolutions@gmail.com',
    '$2a$10$ZCKAP1Wppnq45gh3BGMit.0RPnpiZI5NSDsWhdnPBAoSl2QY2wNVa',
    NULL,
    NULL,
    'MEDECIN'
)
ON CONFLICT (username) DO NOTHING;