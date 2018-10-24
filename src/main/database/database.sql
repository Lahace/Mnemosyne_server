DROP TABLE IF EXISTS mnemosyne.defines;
DROP TABLE IF EXISTS mnemosyne.user;
DROP TABLE IF EXISTS mnemosyne.parameter;
DROP TYPE IF EXISTS param;

CREATE TYPE param AS ENUM('time','location');

CREATE TABLE mnemosyne.user (
    email VARCHAR(255) PRIMARY KEY,
    password TEXT NOT NULL,
    sessionid VARCHAR(32)
);

CREATE TABLE mnemosyne.parameter (
    pname VARCHAR(30) PRIMARY KEY
);

CREATE TABLE mnemosyne.defines(
    email VARCHAR(255) NOT NULL,
    pname VARCHAR(30) NOT NULL,
    type param NOT NULL,
    location POINT,
    time TIME,
    PRIMARY KEY (email, pname),
    FOREIGN KEY (email) REFERENCES mnemosyne.user(email)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (pname) REFERENCES mnemosyne.parameter(pname)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK ((type='time' AND location IS NULL AND time IS NOT NULL) OR (type='location' AND time IS NULL AND location IS NOT NULL))
);