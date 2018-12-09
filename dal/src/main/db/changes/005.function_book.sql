--liquibase formatted sql

--changeset aavasiljev:5 endDelimiter:
CREATE OR REPLACE FUNCTION book(show_id INT, nums INT[], OUT result BOOLEAN)
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
        result = false;
        RETURN;
      END IF;
      curr = set_bit(curr, num - 1, 1);
    END LOOP;
  UPDATE shows SET seats = curr WHERE id = show_id;
  result = true;
  RETURN;
END;
$body$
  LANGUAGE PlPgSQL;
