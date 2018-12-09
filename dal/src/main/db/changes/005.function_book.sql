--liquibase formatted sql

--changeset aavasiljev:5 endDelimiter:
CREATE OR REPLACE FUNCTION book(show_id INT, nums INT[])
  RETURNS BOOLEAN
AS
$body$
DECLARE
  curr bit(100);
  num  INT;
BEGIN
  SELECT seats INTO curr FROM shows WHERE id = show_id;
  FOREACH num IN ARRAY nums
    LOOP
      IF (get_bit(curr, num - 1) = 1) THEN
        RETURN false;
      END IF;
      curr = set_bit(curr, num - 1, 1);
    END LOOP;
  UPDATE shows SET seats = curr WHERE id = show_id;
  RETURN true;
END;
$body$
  LANGUAGE PlPgSQL;
