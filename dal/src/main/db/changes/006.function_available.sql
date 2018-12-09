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
      CONTINUE WHEN ((inp | (B'1' << idx)) = B'1');
      result = array_append(result, ARRAY [idx]);
    END LOOP;
  RETURN result;
END;
$body$
  LANGUAGE PlPgSQL;

--rollback drop function book(show_id INT, nums INT[]);
