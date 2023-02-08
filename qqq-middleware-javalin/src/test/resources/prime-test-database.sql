--
-- QQQ - Low-code Application Framework for Engineers.
-- Copyright (C) 2021-2022.  Kingsrook, LLC
-- 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
-- contact@kingsrook.com
-- https://github.com/Kingsrook/
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as
-- published by the Free Software Foundation, either version 3 of the
-- License, or (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.
--

DROP TABLE IF EXISTS person;
CREATE TABLE person
(
   id INT AUTO_INCREMENT,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),

   first_name VARCHAR(80) NOT NULL,
   last_name VARCHAR(80) NOT NULL,
   birth_date DATE,
   email VARCHAR(250) NOT NULL,
   partner_person_id INT,
   test_script_id INT
);

INSERT INTO person (id, first_name, last_name, birth_date, email, partner_person_id) VALUES (1, 'Darin', 'Kelkhoff', '1980-05-31', 'darin.kelkhoff@gmail.com', 6);
INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (2, 'James', 'Maes', '1980-05-15', 'jmaes@mmltholdings.com');
INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (3, 'Tim', 'Chamberlain', '1976-05-28', 'tchamberlain@mmltholdings.com');
INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (4, 'Tyler', 'Samples', '1990-01-01', 'tsamples@mmltholdings.com');
INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (5, 'Garret', 'Richardson', '1981-01-01', 'grichardson@mmltholdings.com');
INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (6, 'Linda', 'Kelkhoff', '1976-01-01', 'not-linda.kelkhoff@gmail.com');
