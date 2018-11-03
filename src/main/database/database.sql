DROP TABLE IF EXISTS mnemosyne.defines;
DROP TABLE IF EXISTS mnemosyne.task;
DROP TABLE IF EXISTS mnemosyne.user;
DROP TABLE IF EXISTS mnemosyne.found_in;
DROP TABLE IF EXISTS mnemosyne.requires;
DROP TABLE IF EXISTS mnemosyne.wants;
DROP TABLE IF EXISTS mnemosyne.constraint_marker;
DROP TABLE IF EXISTS mnemosyne.constraint_word;
DROP TABLE IF EXISTS mnemosyne.item;
DROP TABLE IF EXISTS mnemosyne.parameter;
DROP TABLE IF EXISTS mnemosyne.verb;
DROP TABLE IF EXISTS mnemosyne.place_type;
DROP TYPE IF EXISTS paramType;
DROP TYPE IF EXISTS timing;
DROP TYPE IF EXISTS norm_act;

CREATE TYPE paramType AS ENUM('time','location');
CREATE TYPE timing AS ENUM('before','at', 'after');
CREATE TYPE norm_act AS ENUM('leave', 'get');

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
    parameter VARCHAR(30) NOT NULL,
    type paramType NOT NULL,
    location POINT,
    time TIME,
    PRIMARY KEY (email, parameter),
    FOREIGN KEY (email) REFERENCES mnemosyne.user(email)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (parameter) REFERENCES mnemosyne.parameter(pname)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK ((type='time' AND location IS NULL AND time IS NOT NULL) OR (type='location' AND time IS NULL AND location IS NOT NULL))
);

CREATE TABLE mnemosyne.task(
    id SERIAL NOT NULL PRIMARY KEY,
    useremail VARCHAR(255) NOT NULL,
    name TEXT NOT NULL,
    constr BYTEA DEFAULT NULL, --serializing java object
    possibleAtWork BOOLEAN NOT NULL,
    repeatable BOOLEAN NOT NULL,
    doneToday BOOLEAN NOT NULL,
    failed BOOLEAN NOT NULL,
    placesToSatisfy BYTEA NOT NULL, --serializing java object
    FOREIGN KEY (useremail) REFERENCES mnemosyne.user(email)
        ON DELETE CASCADE ON UPDATE CASCADE
    --N.B. for future developement, add extra tables to avoid serialization
);

CREATE TABLE mnemosyne.verb(
    word VARCHAR(50) NOT NULL PRIMARY KEY,
    normalized_action norm_act
);

CREATE TABLE mnemosyne.item(
    name varchar(50) NOT NULL PRIMARY KEY
);

CREATE TABLE mnemosyne.requires(
    verb VARCHAR(50) NOT NULL,
    item VARCHAR(50) NOT NULL,
    parameter VARCHAR(30) NOT NULL,
    PRIMARY KEY (verb,item),
    FOREIGN KEY (verb) REFERENCES mnemosyne.verb(word)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (item) REFERENCES mnemosyne.item(name)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (parameter) REFERENCES mnemosyne.parameter(pname)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE mnemosyne.place_type(
    type VARCHAR(50) NOT NULL PRIMARY KEY
);

CREATE TABLE mnemosyne.found_in(
    item VARCHAR(50) NOT NULL,
    place_type VARCHAR(50) NOT NULL,
    PRIMARY KEY(item,place_type),
    FOREIGN KEY (item) REFERENCES mnemosyne.item(name)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (place_type) REFERENCES mnemosyne.place_type(type)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE mnemosyne.constraint_marker(
    marker VARCHAR(20) NOT NULL PRIMARY KEY,
    timing timing NOT NULL
);

CREATE TABLE mnemosyne.constraint_word(
    word VARCHAR(50) NOT NULL PRIMARY KEY
);

CREATE TABLE mnemosyne.wants(
    constraint_marker VARCHAR(20) NOT NULL,
    verb VARCHAR(50) NOT NULL,
    constraint_word VARCHAR(50) NOT NULL,
    parameter VARCHAR(30) NOT NULL,
    PRIMARY KEY (constraint_marker, verb, constraint_word),
    FOREIGN KEY (parameter) REFERENCES mnemosyne.parameter(pname)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (constraint_marker) REFERENCES mnemosyne.constraint_marker(marker)
            ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (constraint_word) REFERENCES mnemosyne.constraint_word(word)
            ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (verb) REFERENCES mnemosyne.verb(word)
                ON DELETE CASCADE ON UPDATE CASCADE
);