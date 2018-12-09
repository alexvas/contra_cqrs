--liquibase formatted sql

--changeset aavasiljev:6 endDelimiter:
CREATE OR REPLACE FUNCTION available(inp bit(100))
  RETURNS INT[]
AS
$body$
DECLARE
  result INT[];
BEGIN
  result = '{}';
  FOR idx IN 0..99
    LOOP
      CONTINUE WHEN get_bit(inp, idx) = 1;
      result = array_append(result, idx + 1);
    END LOOP;
  RETURN result;
END;
$body$
  LANGUAGE PlPgSQL;
