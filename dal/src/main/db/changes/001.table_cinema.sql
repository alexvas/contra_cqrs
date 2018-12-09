--liquibase formatted sql

--changeset aavasiljev:1
CREATE TABLE cinema
(
  id   SERIAL NOT NULL PRIMARY KEY,
  name TEXT   NOT NULL
    CONSTRAINT cinema_name_non_empty CHECK ( name <> '' )
);
