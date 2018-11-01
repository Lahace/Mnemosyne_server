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

CREATE TYPE paramType AS ENUM('time','location');
--CREATE TYPE paramName AS ENUM('location_house', 'location_work', 'location_item', 'location_any',
--                              	'time_lunch', 'time_bed', 'time_dinner', 'time_closure', 'time_work');

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
    type paramType NOT NULL,
    location POINT,
    time TIME,
    PRIMARY KEY (email, pname),
    FOREIGN KEY (email) REFERENCES mnemosyne.user(email)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (pname) REFERENCES mnemosyne.parameter(pname)
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
    word VARCHAR(50) NOT NULL PRIMARY KEY
);

CREATE TABLE mnemosyne.item(
    name varchar(50) NOT NULL PRIMARY KEY
);

CREATE TABLE mnemosyne.requires(
    word VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    pname VARCHAR(30) NOT NULL,
    PRIMARY KEY (word,name),
    FOREIGN KEY (word) REFERENCES mnemosyne.verb(word)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (name) REFERENCES mnemosyne.item(name)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (pname) REFERENCES mnemosyne.parameter(pname)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE mnemosyne.place_type(
    type VARCHAR(50) NOT NULL PRIMARY KEY
);

CREATE TABLE mnemosyne.found_in(
    name VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    PRIMARY KEY(name,type),
    FOREIGN KEY (name) REFERENCES mnemosyne.item(name)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (type) REFERENCES mnemosyne.place_type(type)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE mnemosyne.constraint_marker(
    marker VARCHAR(20) NOT NULL PRIMARY KEY
);

CREATE TABLE mnemosyne.constraint_word(
    word VARCHAR(50) NOT NULL PRIMARY KEY
);

CREATE TABLE mnemosyne.wants(
    pname VARCHAR(30) NOT NULL,
    marker VARCHAR(20) NOT NULL,
    word VARCHAR(50) NOT NULL,
    PRIMARY KEY (pname, marker, word),
    FOREIGN KEY (pname) REFERENCES mnemosyne.parameter(pname)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (marker) REFERENCES mnemosyne.constraint_marker(marker)
            ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (word) REFERENCES mnemosyne.constraint_word(word)
            ON DELETE CASCADE ON UPDATE CASCADE

);