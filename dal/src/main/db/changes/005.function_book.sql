--liquibase formatted sql

--changeset aavasiljev:5 endDelimiter:
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

--rollback drop function book(show_id INT, nums INT[]);
