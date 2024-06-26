/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.sql.parser.hive.ddl;

import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.apache.flink.sql.parser.hive.ddl.SqlCreateHiveTable.HiveTableRowFormat;
import org.apache.calcite.sql.parser.impl.ParseException;

import org.apache.calcite.sql.SqlCharStringLiteral;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;

import static org.apache.flink.sql.parser.hive.ddl.SqlAlterHiveTable.AlterTableOp.CHANGE_SERDE_PROPS;

/** ALTER TABLE DDL to change a Hive table's SerDe. */
public class SqlAlterHiveTableSerDe extends SqlAlterHiveTable {

    private final SqlCharStringLiteral serdeLib;
    private final SqlNodeList origSerDeProps;

    public SqlAlterHiveTableSerDe(
            SqlParserPos pos,
            SqlIdentifier tableName,
            SqlNodeList partitionSpec,
            SqlNodeList propertyList,
            SqlCharStringLiteral serdeLib)
            throws ParseException {
        super(
                CHANGE_SERDE_PROPS,
                pos,
                tableName,
                partitionSpec,
                HiveDDLUtils.checkReservedTableProperties(propertyList));
        HiveDDLUtils.unescapeProperties(propertyList);
        // remove the last property which is the ALTER_TABLE_OP
        origSerDeProps =
                new SqlNodeList(
                        propertyList.getList().subList(0, propertyList.size() - 1),
                        propertyList.getParserPosition());
        appendPrefix(getPropertyList());
        if (serdeLib != null) {
            propertyList.add(
                    HiveDDLUtils.toTableOption(
                            HiveTableRowFormat.SERDE_LIB_CLASS_NAME,
                            serdeLib,
                            serdeLib.getParserPosition()));
        }
        this.serdeLib = serdeLib;
    }

    @Override
    public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
        super.unparse(writer, leftPrec, rightPrec);
        writer.keyword("SET");
        if (serdeLib != null) {
            writer.keyword("SERDE");
            serdeLib.unparse(writer, leftPrec, rightPrec);
        }
        if (origSerDeProps != null && origSerDeProps.size() > 0) {
            if (serdeLib == null) {
                writer.keyword("SERDEPROPERTIES");
            } else {
                writer.keyword("WITH SERDEPROPERTIES");
            }
            SqlWriter.Frame withFrame = writer.startList("(", ")");
            for (SqlNode property : origSerDeProps) {
                printIndent(writer);
                property.unparse(writer, leftPrec, rightPrec);
            }
            writer.newlineAndIndent();
            writer.endList(withFrame);
        }
    }

    private static SqlNodeList appendPrefix(SqlNodeList propList) {
        if (propList != null) {
            for (int i = 0; i < propList.size(); i++) {
                SqlTableOption tableOption = (SqlTableOption) propList.get(i);
                if (!tableOption.getKeyString().equals(ALTER_TABLE_OP)) {
                    String key =
                            HiveTableRowFormat.SERDE_INFO_PROP_PREFIX + tableOption.getKeyString();
                    tableOption =
                            HiveDDLUtils.toTableOption(
                                    key, tableOption.getValue(), tableOption.getParserPosition());
                    propList.set(i, tableOption);
                }
            }
        }
        return propList;
    }
}
