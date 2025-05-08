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

DROP TABLE IF EXISTS user_session;
CREATE TABLE user_session
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   uuid VARCHAR(40) NOT NULL,
   access_token MEDIUMTEXT,
   user_id VARCHAR(100)
);
ALTER TABLE user_session ADD UNIQUE u_uuid (uuid);
ALTER TABLE user_session ADD INDEX i_user_id (user_id);


DROP TABLE IF EXISTS redirect_state;
CREATE TABLE redirect_state
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   create_date TIMESTAMP DEFAULT now(),
   state VARCHAR(45) NOT NULL,
   redirect_uri TEXT
);
ALTER TABLE redirect_state ADD UNIQUE u_state (state);


DROP TABLE IF EXISTS person;
CREATE TABLE person
(
   id INT AUTO_INCREMENT primary key ,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),

   first_name VARCHAR(80) NOT NULL,
   last_name VARCHAR(80) NOT NULL,
   birth_date DATE,
   email VARCHAR(250) NOT NULL,

   is_employed BOOLEAN,
   annual_salary DECIMAL(12, 2),
   days_worked INTEGER
);

INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (1, 'Darin', 'Kelkhoff', '1980-05-31', 'darin.kelkhoff@gmail.com', 1, 75003.50, 1001);
INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (2, 'James', 'Maes', '1980-05-15', 'jmaes@mmltholdings.com', 1, 150000, 10100);
INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (3, 'Tim', 'Chamberlain', '1976-05-28', 'tchamberlain@mmltholdings.com', 1, 300000, 100100);
INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (4, 'Tyler', 'Samples', NULL, 'tsamples@mmltholdings.com', 1, 950000, 75);
INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (5, 'Garret', 'Richardson', '1981-01-01', 'grichardson@mmltholdings.com', 0, 1500000, 1);

DROP TABLE IF EXISTS pet;
CREATE TABLE pet
(
   id INT AUTO_INCREMENT primary key ,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),

   name VARCHAR(80) NOT NULL,
   species_id INTEGER NOT NULL,
   person_id INTEGER NOT NULL,
   birth_date DATE
);

INSERT INTO pet (id, name, species_id, person_id) VALUES (1, 'Charlie', 1, 1);
INSERT INTO pet (id, name, species_id, person_id) VALUES (2, 'Coco', 1, 1);
INSERT INTO pet (id, name, species_id, person_id) VALUES (3, 'Louie', 1, 1);
INSERT INTO pet (id, name, species_id, person_id) VALUES (4, 'Barkley', 1, 1);
INSERT INTO pet (id, name, species_id, person_id) VALUES (5, 'Toby', 1, 2);
INSERT INTO pet (id, name, species_id, person_id) VALUES (6, 'Mae', 2, 3);


DROP TABLE IF EXISTS carrier;
CREATE TABLE carrier
(
   id INT AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR(80) NOT NULL,
   company_code VARCHAR(80) NOT NULL,
   service_level VARCHAR(80) NOT NULL
);

INSERT INTO carrier (id, name, company_code, service_level) VALUES (1, 'UPS Ground', 'UPS', 'G');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (2, 'UPS 2Day', 'UPS', '2');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (3, 'UPS International', 'UPS', 'I');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (4, 'Fedex Ground', 'FEDEX', 'G');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (5, 'Fedex Next Day', 'UPS', '1');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (6, 'Will Call', 'WILL_CALL', 'W');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (7, 'USPS Priority', 'USPS', '1');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (8, 'USPS Super Slow', 'USPS', '4');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (9, 'USPS Super Fast', 'USPS', '0');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (10, 'DHL International', 'DHL', 'I');
INSERT INTO carrier (id, name, company_code, service_level) VALUES (11, 'GSO', 'GSO', 'G');
