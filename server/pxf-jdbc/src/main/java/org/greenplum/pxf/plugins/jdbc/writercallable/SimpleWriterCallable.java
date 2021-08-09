package org.greenplum.pxf.plugins.jdbc.writercallable;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.greenplum.pxf.api.OneRow;
import org.greenplum.pxf.plugins.jdbc.JdbcResolver;
import org.greenplum.pxf.plugins.jdbc.JdbcBasePlugin;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This writer makes simple, one-by-one INSERTs.
 *
 * A call() is required after every supply()
 */
class SimpleWriterCallable implements WriterCallable {
    @Override
    public void supply(OneRow row) throws IllegalStateException {
        if (this.row != null) {
            throw new IllegalStateException("Trying to supply() a OneRow object to a full WriterCallable");
        }
        if (row == null) {
            throw new IllegalArgumentException("Trying to supply() a null OneRow object");
        }
        this.row = row;
    }

    @Override
    public boolean isCallRequired() {
        return this.row != null;
    }

    @Override
    public SQLException call() throws IOException, SQLException, ClassNotFoundException {
        if (row == null) {
            return null;
        }

        boolean statementMustBeDeleted = false;
        if (statement == null) {
            statement = plugin.getPreparedStatement(plugin.getConnection(), query);
            statementMustBeDeleted = true;
        }

        JdbcResolver.decodeOneRowToPreparedStatement(row, statement);

        try {
            statement.executeUpdate();
        } catch (SQLException e) {
            return e;
        } finally {
            row = null;
            if (statementMustBeDeleted) {
                JdbcBasePlugin.closeStatementAndConnection(statement);
                statement = null;
            }
        }

        return null;
    }

    SimpleWriterCallable(JdbcBasePlugin plugin, String query, PreparedStatement statement) {
        if ((plugin == null) || (query == null)) {
            throw new IllegalArgumentException("The provided JdbcBasePlugin or SQL query is null");
        }
        this.plugin = plugin;
        this.query = query;
        this.statement = statement;
        row = null;
    }

    private final JdbcBasePlugin plugin;
    private final String query;
    private PreparedStatement statement;
    private OneRow row;
}
