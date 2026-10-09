```
SET SERVEROUTPUT ON SIZE UNLIMITED

DECLARE
  v_cnt NUMBER;
BEGIN
  FOR t IN (
    SELECT table_name, column_name
    FROM user_tab_columns
    WHERE data_type IN ('NUMBER','VARCHAR2','CHAR','NVARCHAR2')
  ) LOOP
    BEGIN
      EXECUTE IMMEDIATE
        'SELECT COUNT(*) FROM "' || t.table_name ||
        '" WHERE TO_CHAR("' || t.column_name || '") = ''1151388'''
      INTO v_cnt;
      IF v_cnt > 0 THEN
        DBMS_OUTPUT.PUT_LINE(t.table_name || '.' || t.column_name || ' = ' || v_cnt || ' match(es)');
      END IF;
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
  END LOOP;
END;
/

```
