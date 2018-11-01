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

INSERT INTO mnemosyne.task (id, useremail, name, constr, possibleatwork, repeatable, donetoday, failed, placestosatisfy) VALUES (13, 'asd@asd.it', 'Nome', '\xaced00057372002961702e6d6e656d6f73796e652e7265736f75726365732e5461736b54696d65436f6e73747261696e74f839192ac04890450200014c000e636f6e73747261696e7454696d657400154c6a6176612f74696d652f4c6f63616c54696d653b7872002561702e6d6e656d6f73796e652e7265736f75726365732e5461736b436f6e73747261696e74263c35cc9355ceb80200024c0009706172616d4e616d6574001f4c61702f6d6e656d6f73796e652f656e756d732f506172616d734e616d653b4c00047479706574002b4c61702f6d6e656d6f73796e652f656e756d732f436f6e73747261696e7454656d706f72616c547970653b78707e72001d61702e6d6e656d6f73796e652e656e756d732e506172616d734e616d6500000000000000001200007872000e6a6176612e6c616e672e456e756d0000000000000000120000787074000874696d655f6265647e72002961702e6d6e656d6f73796e652e656e756d732e436f6e73747261696e7454656d706f72616c5479706500000000000000001200007871007e0007740004646f706f7372000d6a6176612e74696d652e536572955d84ba1b2248b20c00007870770204ef78', false, false, false, false, '\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a657870000000017704000000017372001c61702e6d6e656d6f73796e652e7265736f75726365732e506f696e7460672b0414d91cff0200024400017844000179787040483851eb851eb8c05ed7ae147ae14878');
INSERT INTO mnemosyne.task (id, useremail, name, constr, possibleatwork, repeatable, donetoday, failed, placestosatisfy) VALUES (14, 'asd@asd.it', 'Nome', '\xaced00057372002a61702e6d6e656d6f73796e652e7265736f75726365732e5461736b506c616365436f6e73747261696e7450ff1f9b7743381b0200014c000f636f6e73747261696e74506c61636574001e4c61702f6d6e656d6f73796e652f7265736f75726365732f506f696e743b7872002561702e6d6e656d6f73796e652e7265736f75726365732e5461736b436f6e73747261696e74263c35cc9355ceb80200024c0009706172616d4e616d6574001f4c61702f6d6e656d6f73796e652f656e756d732f506172616d734e616d653b4c00047479706574002b4c61702f6d6e656d6f73796e652f656e756d732f436f6e73747261696e7454656d706f72616c547970653b78707e72001d61702e6d6e656d6f73796e652e656e756d732e506172616d734e616d6500000000000000001200007872000e6a6176612e6c616e672e456e756d0000000000000000120000787074000e6c6f636174696f6e5f686f7573657e72002961702e6d6e656d6f73796e652e656e756d732e436f6e73747261696e7454656d706f72616c5479706500000000000000001200007871007e00077400057072696d617372001c61702e6d6e656d6f73796e652e7265736f75726365732e506f696e7460672b0414d91cff020002440001784400017978704028000000000000404b000000000000', false, false, false, false, '\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a657870000000017704000000017372001c61702e6d6e656d6f73796e652e7265736f75726365732e506f696e7460672b0414d91cff0200024400017844000179787040483851eb851eb8c05ed7ae147ae14878');

INSERT INTO mnemosyne.verb(word) VALUES ('prendere');
INSERT INTO mnemosyne.verb(word) VALUES ('comprare');
INSERT INTO mnemosyne.verb(word) VALUES ('pagare');
INSERT INTO mnemosyne.verb(word) VALUES ('prenotare');
INSERT INTO mnemosyne.verb(word) VALUES ('dare_mangiare');
INSERT INTO mnemosyne.verb(word) VALUES ('fare');
INSERT INTO mnemosyne.verb(word) VALUES ('mettere');

INSERT INTO mnemosyne.item(name) VALUES ('pane');
INSERT INTO mnemosyne.item(name) VALUES ('medicine');
INSERT INTO mnemosyne.item(name) VALUES ('medicina');
INSERT INTO mnemosyne.item(name) VALUES ('dottore');
INSERT INTO mnemosyne.item(name) VALUES ('gatto');
INSERT INTO mnemosyne.item(name) VALUES ('gatti');
INSERT INTO mnemosyne.item(name) VALUES ('cane');
INSERT INTO mnemosyne.item(name) VALUES ('cani');
INSERT INTO mnemosyne.item(name) VALUES ('crema');
INSERT INTO mnemosyne.item(name) VALUES ('latte');
INSERT INTO mnemosyne.item(name) VALUES ('bollo');
INSERT INTO mnemosyne.item(name) VALUES ('lavatrice');

INSERT INTO mnemosyne.place_type(type) VALUES ('bakery');
INSERT INTO mnemosyne.place_type(type) VALUES ('supermarket');
INSERT INTO mnemosyne.place_type(type) VALUES ('pharmacy');
INSERT INTO mnemosyne.place_type(type) VALUES ('bar');
INSERT INTO mnemosyne.place_type(type) VALUES ('greengrocer');
INSERT INTO mnemosyne.place_type(type) VALUES ('fuel');
INSERT INTO mnemosyne.place_type(type) VALUES ('hardware');
INSERT INTO mnemosyne.place_type(type) VALUES ('post office');
INSERT INTO mnemosyne.place_type(type) VALUES ('post box');
INSERT INTO mnemosyne.place_type(type) VALUES ('pastry');
INSERT INTO mnemosyne.place_type(type) VALUES ('fast food');
INSERT INTO mnemosyne.place_type(type) VALUES ('clinic');
INSERT INTO mnemosyne.place_type(type) VALUES ('butcher');
INSERT INTO mnemosyne.place_type(type) VALUES ('dairy');
INSERT INTO mnemosyne.place_type(type) VALUES ('mall');
INSERT INTO mnemosyne.place_type(type) VALUES ('medical_supply');
INSERT INTO mnemosyne.place_type(type) VALUES ('electronics');

INSERT INTO mnemosyne.found_in(name,type) VALUES ('pane', 'bakery');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('pane', 'supermarket');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('medicina', 'pharmacy');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('medicina', 'medical_supply');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('medicina', 'mall');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('medicina', 'supermarket');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('latte', 'dairy');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('latte', 'supermarket');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('latte', 'mall');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('crema', 'mall');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('crema', 'supermarket');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('crema', 'pharmacy');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('bollo', 'bar');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('bollo', 'post office');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('lavatrice', 'mall');
INSERT INTO mnemosyne.found_in(name,type) VALUES ('lavatrice', 'electronics');

INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('prendere','pane','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('prendere','latte','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('prendere','medicina','location_house');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('prendere','medicine','location_house');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('prendere','crema','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('prendere','lavatrice','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('comprare','pane','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('comprare','latte','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('comprare','medicina','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('comprare','medicine','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('comprare','crema','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('comprare','lavatrice','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('pagare','bollo','location_item');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('prenotare','dottore','location_house');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('dare_mangiare','gatto','location_house');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('dare_mangiare','cane','location_house');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('dare_mangiare','gatti','location_house');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('dare_mangiare','cani','location_house');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('fare','lavatrice','location_house');
INSERT INTO mnemosyne.requires(word,name,pname) VALUES ('mettere','crema','location_house');


--SELECT ST_X(location) as x_coordinate, ST_Y(location) as y_coordinate FROM defines WHERE location IS NOT NULL