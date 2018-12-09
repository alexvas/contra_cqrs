--liquibase formatted sql

--changeset aavasiljev:2
CREATE TABLE hall
(
  id          SERIAL NOT NULL PRIMARY KEY,
  num         INT    NOT NULL CHECK ( 0 < num ),
  cinema_id   INT    NOT NULL
    CONSTRAINT hall_cinema_ref REFERENCES cinema DEFERRABLE,
  seats_count INT    NOT NULL CHECK ( 0 < seats_count AND seats_count < 101 )
);
CREATE INDEX hall_cinema_key
  ON hall (cinema_id);
