CREATE DATABASE IF NOT EXISTS medilabosolutions;
USE medilabosolutions;

CREATE TABLE patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender CHAR(1) NOT NULL,
    address VARCHAR(100),
    phone VARCHAR(20),
    early_onset_mail_sent BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO patient (first_name, last_name, birth_date, gender, address, phone, early_onset_mail_sent) VALUES
    ('TestNone', 'Test', '1966-12-31', 'F', '1 Brookside St', '100-222-3333', FALSE),
    ('TestBorderline', 'Test', '1945-06-24', 'M', '2 High St', '200-333-4444', FALSE),
    ('TestInDanger', 'Test', '2004-06-18', 'M', '3 Club Road', '300-444-5555', FALSE),
    ('TestEarlyOnset', 'Test', '2002-06-28', 'F', '4 Valley Dr', '400-555-6666', FALSE);
