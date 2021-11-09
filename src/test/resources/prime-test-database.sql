DROP TABLE IF EXISTS person;
CREATE TABLE person
(
   id SERIAL,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),

   first_name VARCHAR(80) NOT NULL,
   last_name VARCHAR(80) NOT NULL,
   birth_date DATE,
   email VARCHAR(250) NOT NULL
);

INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (1, 'Darin', 'Kelkhoff', '1980-05-31', 'darin.kelkhoff@gmail.com');
INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (2, 'James', 'Maes', '1980-05-15', 'jmaes@mmltholdings.com');
INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (3, 'Tim', 'Chamberlain', '1976-05-28', 'tchamberlain@mmltholdings.com');
INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (4, 'Tyler', 'Samples', '1990-01-01', 'tsamples@mmltholdings.com');
INSERT INTO person (id, first_name, last_name, birth_date, email) VALUES (5, 'Garret', 'Richardson', '1981-01-01', 'grichardson@mmltholdings.com');
