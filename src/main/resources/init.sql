CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE articles (
  id         VARCHAR DEFAULT uuid_generate_v1(),
  title      VARCHAR(256),
  keywords   VARCHAR(256),
  content    TEXT,
  url        VARCHAR(128),
  student_id INT,
  PRIMARY KEY (id)
);

CREATE TABLE students (
  id      INT,
  name    VARCHAR(32),
  surname VARCHAR(32),
  mygroup VARCHAR(6),
  PRIMARY KEY (id)
);

INSERT INTO students (id, name, surname, mygroup)
  VALUES (107, 'Камиль', 'Каримов', '11-502');