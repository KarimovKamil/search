CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS articles (
  id         UUID DEFAULT uuid_generate_v1(),
  title      VARCHAR(256),
  keywords   VARCHAR(256),
  content    TEXT,
  url        VARCHAR(128),
  student_id INT,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS students (
  id      INT,
  name    VARCHAR(32),
  surname VARCHAR(32),
  mygroup VARCHAR(6),
  PRIMARY KEY (id)
);

INSERT INTO students (id, name, surname, mygroup)
  VALUES (107, 'Камиль', 'Каримов', '11-502');

CREATE TABLE IF NOT EXISTS words_porter (
  id UUID DEFAULT uuid_generate_v1(),
  term VARCHAR(64),
  articles_id VARCHAR,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS words_mystem (
  id UUID DEFAULT uuid_generate_v1(),
  term VARCHAR(64),
  articles_id VARCHAR,
  PRIMARY KEY (id)
);