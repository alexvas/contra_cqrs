--liquibase formatted sql

-- https://github.com/pgjdbc/pgjdbc/issues/908

--changeset aavasiljev:5 endDelimiter:
CREATE OR REPLACE FUNCTION book(show_id INT, nums INT[], OUT result BOOLEAN)
AS
$body$
DECLARE
  zeros   bit(100);
  curr    bit(100);
  num     INT;
  updated INT;
BEGIN
  zeros = B'0'::bit(100);
  curr = zeros;
  FOREACH num IN ARRAY nums
    LOOP
      curr = set_bit(curr, num - 1, 1);
    END LOOP;
  WITH rowCount AS (
    UPDATE shows SET seats = seats | curr
      WHERE id = show_id AND seats & curr = zeros
      RETURNING 1
  )
  SELECT count(*) INTO updated
  from rowCount;
  result = updated > 0;
  RETURN;
END;
$body$
  LANGUAGE PlPgSQL;