-- @description query01 for list ORC data types
\pset null 'NIL'

SELECT * FROM pxf_orc_list_types ORDER BY id;

SELECT id, bool_arr[1] FROM pxf_orc_list_types ORDER BY id;