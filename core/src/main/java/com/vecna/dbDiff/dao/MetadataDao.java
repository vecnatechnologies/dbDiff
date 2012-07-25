/**
 * Copyright 2011 Vecna Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
*/

package com.vecna.dbDiff.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.TableType;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.db.ForeignKey;
import com.vecna.dbDiff.model.db.Table;


/**
 * @author dlopuch@vecna.com
 */
public interface MetadataDao {

  /**
   * Gets a list of catalog tables for the JDBC connection
   * @param catalogSchema a desired catalog and schema, if any. May be null.
   * @param type A type of table to get
   * @return a Set of Tables
   * @throws SQLException If exception retrieving names
   */
  Set<Table> getTables(CatalogSchema catalogSchema, TableType type) throws SQLException;

  /**
   * Gets all of the columns for a Table
   * @param table a particular Table to search.  Should have catalog, schema, and name set.
   * @return A List of columns, sorted by ordinal.
   * @throws SQLException If exception
   */
  List<Column> getColumns(Table table) throws SQLException;

  /**
   * Gets all the foreign keys in a table (ie all the keys pointing from the specified table to some other table)
   * @param table an importing table
   * @return A List of foreign keys, sorted by referenced catalog, referenced schema, referenced name, and key sequence
   * @throws SQLException
   */
  List<ForeignKey> getForeignKeys(Table table) throws SQLException;

  /**
   * Returns the String corresponding to the supplied TableType in the particular db implementation
   * @param tableType some TableType
   * @return A String for the particular TableType in the particular implementation
   */
  String getTableTypeStr(TableType tableType);

  /**
   * Returns the list of primary key columns
   * @param table the table to retrieve pk information for
   * @return the list of primary key columns if the table has a primary key or null if it doesn't
   * @throws SQLException
   */
  List<String> getPrimaryKeyColumns(Table table) throws SQLException;
}
