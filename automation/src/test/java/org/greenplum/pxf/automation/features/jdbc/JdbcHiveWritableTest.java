package org.greenplum.pxf.automation.features.jdbc;

import jsystem.framework.system.SystemManagerImpl;
import org.greenplum.pxf.automation.components.hive.Hive;
import org.greenplum.pxf.automation.features.BaseFeature;
import org.greenplum.pxf.automation.structures.tables.basic.Table;
import org.greenplum.pxf.automation.structures.tables.hive.HiveExternalTable;
import org.greenplum.pxf.automation.structures.tables.hive.HiveTable;
import org.greenplum.pxf.automation.structures.tables.pxf.ExternalTable;
import org.greenplum.pxf.automation.structures.tables.utils.TableFactory;
import org.testng.annotations.Test;

import java.io.File;

public class JdbcHiveWritableTest extends BaseFeature {

    private ExternalTable pxfJdbcHiveWritable;
    private ExternalTable pxfJdbcHiveReadable;

    private Hive hive;
    private HiveTable hiveTable;

    private static final String HIVE_JDBC_DRIVER_CLASS = "org.apache.hive.jdbc.HiveDriver";
    private static final String[] GPDB_TYPES_TABLE_FIELDS = new String[] {
            "id       int",
            "greeting text"
    };

    private static final String[] HIVE_TYPES_TABLE_FIELDS = new String[] {
            "id       int",
            "greeting string",

    };

    @Override
    protected void beforeClass() throws Exception {
        hive = (Hive) SystemManagerImpl.getInstance().getSystemObject("hive");
        hiveTable = TableFactory.getHiveByRowCommaTable("hive_pxf_jdbc_target", HIVE_TYPES_TABLE_FIELDS);
        hive.createTableAndVerify(hiveTable);

        pxfJdbcHiveWritable = TableFactory.getPxfJdbcWritableTable(
                "pxf_jdbc_hive_writable",
                GPDB_TYPES_TABLE_FIELDS,
                hiveTable.getFullName(),
                HIVE_JDBC_DRIVER_CLASS,
                String.format("jdbc:hive2://%s:10000/default", hive.getHost()),
                null,
                null
        );
        pxfJdbcHiveWritable.setHost(pxfHost);
        pxfJdbcHiveWritable.setPort(pxfPort);
        gpdb.createTableAndVerify(pxfJdbcHiveWritable);

        pxfJdbcHiveReadable = TableFactory.getPxfJdbcReadableTable(
                "pxf_jdbc_hive_readable",
                GPDB_TYPES_TABLE_FIELDS,
                hiveTable.getFullName(),
                HIVE_JDBC_DRIVER_CLASS,
                String.format("jdbc:hive2://%s:10000/default", hive.getHost()),
                null
        );
        pxfJdbcHiveReadable.setHost(pxfHost);
        pxfJdbcHiveReadable.setPort(pxfPort);
        gpdb.createTableAndVerify(pxfJdbcHiveReadable);

    }

    @Override
    protected void afterClass() {
        if (hive != null) {
            hive.close();
        }
    }

    @Test(groups = {"features", "gpdb", "security"})
    public void jdbcHiveWrite() throws Exception {
        runTincTest("pxf.features.jdbc.hive_writable.runTest");
    }
}
