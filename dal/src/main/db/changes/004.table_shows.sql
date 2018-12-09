--liquibase formatted sql

--changeset aavasiljev:4
CREATE TABLE shows
(
  id       SERIAL       NOT NULL PRIMARY KEY,
  start    TIMESTAMP NOT NULL,
  hall_id  INT       NOT NULL
    CONSTRAINT show_hall_ref REFERENCES hall DEFERRABLE,
  movie_id INT       NOT NULL
    CONSTRAINT show_movie_ref REFERENCES movie DEFERRABLE,
  seats    BIT(100)  NOT NULL
);
CREATE INDEX show_hall_key
  ON shows (hall_id);
CREATE INDEX show_movie_key
  ON shows (movie_id);
