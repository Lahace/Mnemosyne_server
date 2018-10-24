INSERT INTO mnemosyne.user(email, password) VALUES('asd@asd.it', crypt('password', gen_salt('bf', 8)));
INSERT INTO mnemosyne.user(email, password) VALUES('asd2@asd.it', crypt('password', gen_salt('bf', 8)));
INSERT INTO mnemosyne.user(email, password) VALUES('asd3@asd.it', crypt('password', gen_salt('bf', 8)));

INSERT INTO mnemosyne.parameter(pname) VALUES('location_house');
INSERT INTO mnemosyne.parameter(pname) VALUES('location_work');
INSERT INTO mnemosyne.parameter(pname) VALUES('location_item');
INSERT INTO mnemosyne.parameter(pname) VALUES('location_any');
INSERT INTO mnemosyne.parameter(pname) VALUES('time_lunch');
INSERT INTO mnemosyne.parameter(pname) VALUES('time_bed');
INSERT INTO mnemosyne.parameter(pname) VALUES('time_dinner');
INSERT INTO mnemosyne.parameter(pname) VALUES('time_closure');

INSERT INTO mnemosyne.defines(email, pname, type, location, time) VALUES ('asd@asd.it','time_lunch','time',NULL,'12:00');
INSERT INTO mnemosyne.defines(email, pname, type, location, time) VALUES ('asd@asd.it','location_house','location', point(45.703280,11.356476),NULL);

--SELECT ST_X(location) as x_coordinate, ST_Y(location) as y_coordinate FROM defines WHERE location IS NOT NULL