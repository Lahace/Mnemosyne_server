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
-- Find badge, surname, age, and salary of the employees who earn more than 42
--

SELECT *
  FROM Ferro.Employee
  WHERE salary > 42;


--
-- Find badge, surname, and age the employees who earn more than 42
--
--

SELECT badge, surname, age
  FROM Ferro.Employee
  WHERE salary > 42;


--
-- Find the badge of the managers whose employees earn more than 42
--

SELECT M.manager
  FROM Ferro.Employee AS E INNER JOIN Ferro.Manage AS M ON E.badge = M.employee
  WHERE E.salary > 40;


--
-- Finda surname and salary of the manages whose employees earn more than 42
--

SELECT ME.surname, ME.salary
  FROM Ferro.Employee AS E INNER JOIN Ferro.Manage AS M ON E.badge = M.employee INNER JOIN Ferro.Employee AS ME ON M.manager = ME.badge
  WHERE E.salary > 42;


--
-- Find the surname and salary of the managers who earn more than 45
--

SELECT DISTINCT surname, salary
  FROM Ferro.Employee AS E INNER JOIN Ferro.Manage AS M ON E.badge = M.manager
  WHERE salary > 45;
  

--
-- Find the employees who earn more than their manager and show badge, surname and 
-- salary of both the employee and the manager
--

SELECT E.badge, E.surname, E.salary, ME.badge, ME.surname, ME.salary
	FROM Ferro.Employee AS E INNER JOIN Ferro.Manage AS M ON E.badge = M.employee INNER JOIN Ferro.Employee AS ME ON M.manager = ME.badge
	WHERE E.salary > ME.salary;
	

--
-- Find the badge of the managers whose employees ALL earn more than 42
--

SELECT manager
  FROM Ferro.Manage
EXCEPT
SELECT manager
  FROM Ferro.Employee AS E INNER JOIN Ferro.Manage AS M ON E.badge = M.employee 
  WHERE E.salary <= 42;


--
-- Find the badge of the managers who, in turn, have a manager
--

SELECT DISTINCT M.employee
  FROM Ferro.Manage AS M INNER JOIN Ferro.Manage AS MM ON M.employee = MM.manager;

-- or

SELECT employee
  FROM Ferro.Manage
INTERSECT
SELECT manager
  FROM Ferro.Manage;

