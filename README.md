```

docker exec -it oracle sqlplus sys/12345@XEPDB1 as sysdba

-- check grants
SELECT grantee, privilege, table_name 
FROM dba_tab_privs 
WHERE grantee IN ('TAM', 'PVM')
AND table_name IN ('DBA_PENDING_TRANSACTIONS', 'PENDING_TRANS$', 'DBA_2PC_PENDING');

SELECT grantee, privilege 
FROM dba_sys_privs 
WHERE grantee IN ('TAM', 'PVM');



GRANT SELECT ON sys.dba_pending_transactions TO your_db_user;
GRANT SELECT ON sys.pending_trans$ TO your_db_user;
GRANT SELECT ON sys.dba_2pc_pending TO your_db_user;
GRANT EXECUTE ON sys.dbms_xa TO your_db_user;


GRANT SELECT ON sys.dba_pending_transactions TO your_user;
GRANT SELECT ON sys.pending_trans$ TO your_user;
GRANT SELECT ON sys.dba_2pc_pending TO your_user;
GRANT EXECUTE ON sys.dbms_xa TO your_user;

```
