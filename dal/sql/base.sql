begin;

CREATE TABLE cinema
(
  id   SERIAL NOT NULL PRIMARY KEY,
  name TEXT   NOT NULL
    CONSTRAINT cinema_name_non_empty CHECK ( name <> '' )
);

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

CREATE TABLE movie
(
  id    SERIAL  NOT NULL PRIMARY KEY,
  title TEXT NOT NULL
    CONSTRAINT movie_title_non_empty CHECK ( title <> '' )
);

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

CREATE OR REPLACE FUNCTION book(show_id INT, nums INT[])
  RETURNS boolean
AS
$body$
DECLARE
  curr     INT;
  combined INT;
BEGIN
  FOREACH curr IN ARRAY nums
    LOOP
      combined = combined | (B'1' << (curr - 1));
    END LOOP;
  UPDATE shows SET seats = seats | combined WHERE id = show_id AND (seats & combined)::INT = 0;
END;
$body$
  LANGUAGE PlPgSQL;


commit;