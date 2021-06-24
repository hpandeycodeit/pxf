-- @description query01 for JDBC Hive writing
INSERT INTO pxf_jdbc_hive_writable VALUES (1, 'hello pxf');
SELECT * FROM pxf_jdbc_hive_readable ORDER BY id;