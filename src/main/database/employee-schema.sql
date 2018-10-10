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


-- #################################################################################################
-- ## Creation of a schema to avoid name clashes                                                  ##
-- #################################################################################################

-- Drop the ferro schema, if exists, and any object within it
DROP SCHEMA IF EXISTS ferro CASCADE;

-- Create the ferro schema
CREATE SCHEMA ferro;
COMMENT ON SCHEMA ferro IS 'Schema for containing the objects of the user ferro';


-- #################################################################################################
-- ## Creation of the tables                                                                      ##
-- #################################################################################################

--
-- This table represents and Employee
--
-- Version 1.00
CREATE TABLE Ferro.Employee (
	badge INT,
	surname VARCHAR(50) NOT NULL,
	age INT,
	salary INT,
	PRIMARY KEY (badge)
);

COMMENT ON TABLE Ferro.Employee IS 'Represents an employee.';
COMMENT ON COLUMN Ferro.Employee.badge IS 'The unique badge number of an employee.';
COMMENT ON COLUMN Ferro.Employee.surname IS 'The surname of the employee.';
COMMENT ON COLUMN Ferro.Employee.age IS 'The age of the employee.';
COMMENT ON COLUMN Ferro.Employee.salary IS 'The salary of the employee in thousands of euros.';

--
-- The table represents the manager of an employee
--
-- Version 1.00
CREATE TABLE Ferro.Manage (
  employee INT NOT NULL,
  manager INT NOT NULL,
  PRIMARY KEY (employee),
  FOREIGN KEY (employee) REFERENCES Ferro.Employee(badge),
  FOREIGN KEY (manager) REFERENCES Ferro.Employee(badge)
);

COMMENT ON TABLE Ferro.Manage IS 'Represents the manager of an employee.';
COMMENT ON COLUMN Ferro.Manage.employee IS 'Badge number of the managed employee.';
COMMENT ON COLUMN Ferro.Manage.manager IS 'Badge number of the manager of the employee.';


