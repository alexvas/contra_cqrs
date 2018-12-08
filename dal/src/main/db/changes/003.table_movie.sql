--liquibase formatted sql

--changeset aavasiljev:3
CREATE TABLE movie
(
  id    SERIAL  NOT NULL PRIMARY KEY,
  title TEXT NOT NULL
    CONSTRAINT movie_title_non_empty CHECK ( title <> '' )
);
--rollback drop table movie;
