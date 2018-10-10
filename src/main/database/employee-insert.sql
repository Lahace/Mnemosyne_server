--  Copyright 2018 University of Padua, Italy
--  
--  Licensed under the Apache License, Version 2.0 (the "License");
--  you may not use this file except in compliance with the License.
--  You may obtain a copy of the License at
--  
--      http://www.apache.org/licenses/LICENSE-2.0
--  
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--  See the License for the specific language governing permissions and
--  limitations under the License.
--  
--  Author: Nicola Ferro (ferro@dei.unipd.it)
--  Version: 1.0
--  Since: 1.0
--
-- Insertion into the Employee table
--
INSERT INTO Ferro.Employee VALUES (7309, 'Rossi', 34, 45);
INSERT INTO Ferro.Employee VALUES (5998, 'Bianchi', 37, 38);
INSERT INTO Ferro.Employee VALUES (9553, 'Neri', 42, 35);
INSERT INTO Ferro.Employee VALUES (5698, 'Bruni', 43, 42);
INSERT INTO Ferro.Employee VALUES (4076, 'Mori', 45, 50);
INSERT INTO Ferro.Employee VALUES (8123, 'Lupi', 46, 60);

--
-- Insertion into the Manage table
--
INSERT INTO Ferro.Manage VALUES(7309, 5698);
INSERT INTO Ferro.Manage VALUES(5998, 5698);
INSERT INTO Ferro.Manage VALUES(9553, 4076);
INSERT INTO Ferro.Manage VALUES(5698, 4076);
INSERT INTO Ferro.Manage VALUES(4076, 8123);
