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
   id INT AUTO_INCREMENT primary key ,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),

   first_name VARCHAR(80) NOT NULL,
   last_name VARCHAR(80) NOT NULL,
   birth_date DATE,
   email VARCHAR(250) NOT NULL,
   is_employed BOOLEAN,
   annual_salary DECIMAL(12,2),
   days_worked INTEGER
);

INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (1, 'Darin', 'Kelkhoff', '1980-05-31', 'darin.kelkhoff@gmail.com', 1, 25000, 27);
INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (2, 'James', 'Maes', '1980-05-15', 'jmaes@mmltholdings.com', 1, 26000, 124);
INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (3, 'Tim', 'Chamberlain', '1976-05-28', 'tchamberlain@mmltholdings.com', 0, null, 0);
INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (4, 'Tyler', 'Samples', NULL, 'tsamples@mmltholdings.com', 1, 30000, 99);
INSERT INTO person (id, first_name, last_name, birth_date, email, is_employed, annual_salary, days_worked) VALUES (5, 'Garret', 'Richardson', '1981-01-01', 'grichardson@mmltholdings.com', 1, 1000000, 232);

DROP TABLE IF EXISTS personal_id_card;
CREATE TABLE personal_id_card
(
   id INT AUTO_INCREMENT primary key ,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   person_id INTEGER,
   id_number VARCHAR(250)
);

INSERT INTO personal_id_card (person_id, id_number) VALUES (1, '19800531');
INSERT INTO personal_id_card (person_id, id_number) VALUES (2, '19800515');
INSERT INTO personal_id_card (person_id, id_number) VALUES (3, '19760528');
INSERT INTO personal_id_card (person_id, id_number) VALUES (6, '123123123');
INSERT INTO personal_id_card (person_id, id_number) VALUES (null, '987987987');
INSERT INTO personal_id_card (person_id, id_number) VALUES (null, '456456456');

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

DROP TABLE IF EXISTS line_item_extrinsic;
DROP TABLE IF EXISTS order_line;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS warehouse_store_int;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS warehouse;

CREATE TABLE store
(
   id INT AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR(80) NOT NULL
);

-- define 3 stores
INSERT INTO store (id, name) VALUES (1, 'Q-Mart');
INSERT INTO store (id, name) VALUES (2, 'QQQ ''R'' Us');
INSERT INTO store (id, name) VALUES (3, 'QDepot');

CREATE TABLE item
(
   id INT AUTO_INCREMENT PRIMARY KEY,
   sku VARCHAR(80) NOT NULL,
   store_id INT NOT NULL REFERENCES store
);

-- three items for each store
INSERT INTO item (id, sku, store_id) VALUES (1, 'QM-1', 1);
INSERT INTO item (id, sku, store_id) VALUES (2, 'QM-2', 1);
INSERT INTO item (id, sku, store_id) VALUES (3, 'QM-3', 1);
INSERT INTO item (id, sku, store_id) VALUES (4, 'QRU-1', 2);
INSERT INTO item (id, sku, store_id) VALUES (5, 'QRU-2', 2);
INSERT INTO item (id, sku, store_id) VALUES (6, 'QRU-3', 2);
INSERT INTO item (id, sku, store_id) VALUES (7, 'QD-1', 3);
INSERT INTO item (id, sku, store_id) VALUES (8, 'QD-2', 3);
INSERT INTO item (id, sku, store_id) VALUES (9, 'QD-3', 3);

CREATE TABLE `order`
(
   id INT AUTO_INCREMENT PRIMARY KEY,
   store_id INT REFERENCES store,
   bill_to_person_id INT,
   ship_to_person_id INT
);

-- variable orders
INSERT INTO `order` (id, store_id, bill_to_person_id, ship_to_person_id) VALUES (1, 1, 1, 1);
INSERT INTO `order` (id, store_id, bill_to_person_id, ship_to_person_id) VALUES (2, 1, 1, 2);
INSERT INTO `order` (id, store_id, bill_to_person_id, ship_to_person_id) VALUES (3, 1, 2, 3);
INSERT INTO `order` (id, store_id, bill_to_person_id, ship_to_person_id) VALUES (4, 2, 4, 5);
INSERT INTO `order` (id, store_id, bill_to_person_id, ship_to_person_id) VALUES (5, 2, 5, 4);
INSERT INTO `order` (id, store_id, bill_to_person_id, ship_to_person_id) VALUES (6, 3, 5, null);
INSERT INTO `order` (id, store_id, bill_to_person_id, ship_to_person_id) VALUES (7, 3, null, 5);
INSERT INTO `order` (id, store_id, bill_to_person_id, ship_to_person_id) VALUES (8, 3, null, 5);

CREATE TABLE order_line
(
   id INT AUTO_INCREMENT PRIMARY KEY,
   order_id INT REFERENCES `order`,
   sku VARCHAR(80),
   store_id INT REFERENCES store,
   quantity INT
);

-- various lines
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (1, 'QM-1', 1, 10);
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (1, 'QM-2', 1, 1);
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (1, 'QM-3', 1, 1);
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (2, 'QRU-1', 2, 1); -- this line has an item from a different store than its order.
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (3, 'QM-1', 1, 20);
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (4, 'QRU-1', 2, 1);
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (4, 'QRU-2', 2, 2);
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (5, 'QRU-1', 2, 1);
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (6, 'QD-1', 3, 1);
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (7, 'QD-1', 3, 2);
INSERT INTO order_line (order_id, sku, store_id, quantity) VALUES (8, 'QD-1', 3, 3);


CREATE TABLE warehouse
(
   id INT AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR(80)
);

INSERT INTO warehouse (name) VALUES ('Patterson');
INSERT INTO warehouse (name) VALUES ('Edison');
INSERT INTO warehouse (name) VALUES ('Stockton');
INSERT INTO warehouse (name) VALUES ('Somewhere in Texas');

CREATE TABLE warehouse_store_int
(
   id INT AUTO_INCREMENT PRIMARY KEY,
   warehouse_id INT REFERENCES `warehouse`,
   store_id INT REFERENCES `store`
);

INSERT INTO warehouse_store_int (warehouse_id, store_id) VALUES (1, 1);
INSERT INTO warehouse_store_int (warehouse_id, store_id) VALUES (1, 2);
INSERT INTO warehouse_store_int (warehouse_id, store_id) VALUES (1, 3);

CREATE TABLE line_item_extrinsic
(
   id INT AUTO_INCREMENT PRIMARY KEY,
   order_line_id INT REFERENCES order_line,
   `key` VARCHAR(80),
   `value` VARCHAR(80)
);

