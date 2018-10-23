DROP TABLE IF EXISTS user;

CREATE TABLE mnemosyne.user (
    email VARCHAR(255) NOT NULL,
    password VARCHAR(64) NOT NULL,
    sessionid VARCHAR(32),
    PRIMARY KEY (email)
) CHARACTER SET utf8;