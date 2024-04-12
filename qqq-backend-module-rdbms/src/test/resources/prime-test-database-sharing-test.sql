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

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   username VARCHAR(100)
);


DROP TABLE IF EXISTS `group`;
CREATE TABLE `group`
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   name VARCHAR(100),
   client_id INTEGER
);


DROP TABLE IF EXISTS `client`;
CREATE TABLE `client`
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   name VARCHAR(100)
);


DROP TABLE IF EXISTS audience;
CREATE TABLE audience
(
   id INT AUTO_INCREMENT primary key ,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   type VARCHAR(50),
   name VARCHAR(100),
   security_key VARCHAR(100)
);


DROP TABLE IF EXISTS asset;
CREATE TABLE asset
(
   id INT AUTO_INCREMENT primary key ,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   name VARCHAR(100),
   user_id INTEGER
);


DROP TABLE IF EXISTS asset_audience_int;
CREATE TABLE asset_audience_int
(
   id INT AUTO_INCREMENT primary key ,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   asset_id INTEGER,
   audience_id INTEGER
);

