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

package com.vecna.dbDiff.dao.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vecna.dbDiff.dao.MetadataDao;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.ColumnType;
import com.vecna.dbDiff.model.TableType;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.db.ForeignKey;
import com.vecna.dbDiff.model.db.Table;

/**
 * @author dlopuch@vecna.com
 */
public class GenericMetadataDaoImpl implements MetadataDao {

  private DatabaseMetaData m_metadata;

  /**
   * Constructor that sets metadata based on a JDBC connection
   * @param connection some active JDBC connection
   */
  public GenericMetadataDaoImpl(Connection connection) {
    try {
      setMetadata(connection.getMetaData());
    } catch (SQLException e) {
      throw new RuntimeException("Unable to grab metadata to initialize GenericMetadataDaoImpl: " + e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Table> getTables(CatalogSchema catalogSchema, TableType type) throws SQLException {
    // Get the ResultSet of tables
    String[] tableTypes = {this.getTableTypeStr(type)};
    ResultSet rs = doGetTablesQuery(catalogSchema, tableTypes);

    // Build a set of Tables
    Set<Table> tables = new HashSet<Table>();
    while (rs.next()) {
      Table t = new Table();
      t.setCatalog(rs.getString(1));
      t.setSchema(rs.getString(2));
      t.setName(rs.getString(3));
      t.setType(rs.getString(4));
      t.setTypeName(rs.getString(5));

      //Get index names
      ResultSet irs = getMetadata().getIndexInfo(catalogSchema.getCatalog(), catalogSchema.getSchema(),
                                                 rs.getString(3), false, false);
      Set<String> indexNames = Sets.newHashSet();
      while (irs.next()) {
        indexNames.add(irs.getString(6));
      }
      if (!indexNames.isEmpty()) {
        t.setIndexNames(indexNames);
      }
      tables.add(t);
    }
    return tables;
  }

  /**
   * Performs a metaData.getTables() query
   * @param catalogSchema the desired catalog and schema names
   * @param tableTypes the desired table types, specific for the particular implementation
   * @return The ResultSet of the getTables() call
   * @throws SQLException
   */
  protected ResultSet doGetTablesQuery(CatalogSchema catalogSchema, String[] tableTypes) throws SQLException {
    return getMetadata().getTables((catalogSchema == null ? null : catalogSchema.getCatalog()),
                                   (catalogSchema == null ? null : catalogSchema.getSchema()),
                                   null, tableTypes);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public List<Column> getColumns(Table table) throws SQLException {
    ResultSet rs = getMetadata().getColumns(table.getCatalog(), table.getSchema(), table.getName(), null);

    List<Column> cols = new LinkedList<Column>();
    while (rs.next()) {
      Column c = new Column();
      c.setCatalog(rs.getString(1));
      c.setSchema(rs.getString(2));
      c.setTable(rs.getString(3));

      c.setName(rs.getString(4));
      c.setColumnType(new ColumnType(rs.getInt(5), rs.getString(6)));
      c.setColumnSize(rs.getInt(7));

      //Nullability
      int nullable = rs.getInt(11);
      c.setIsNullable((DatabaseMetaData.columnNullable == nullable ? true :
        (DatabaseMetaData.columnNoNulls == nullable ? false : null)));

      c.setDefault(rs.getString(13));
      c.setOrdinal(rs.getInt(17));
      cols.add(c);
    }
    return cols;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ForeignKey> getForeignKeys(Table table) throws SQLException {
    ResultSet rs = getMetadata().getImportedKeys(table.getCatalog(), table.getSchema(), table.getName());
    List<ForeignKey> fks = new LinkedList<ForeignKey>();
    while (rs.next()) {
      ForeignKey fk = new ForeignKey();
      fk.setFkName(rs.getString(12));

      fk.setFkCatalog(rs.getString(5));
      fk.setFkSchema(rs.getString(6));
      fk.setFkTable(rs.getString(7));
      fk.setFkColumn(rs.getString(8));

      fk.setPkCatalog(rs.getString(1));
      fk.setPkSchema(rs.getString(2));
      fk.setPkTable(rs.getString(3));
      fk.setPkColumn(rs.getString(4));

      fk.setKeySeq(rs.getString(9));
      fks.add(fk);
    }
    return fks;
  }


  /**
   * {@inheritDoc}
   * May be overridden by specific implementations
   */
  @Override
  public String getTableTypeStr(TableType tableType) {
    switch (tableType) {
      case TABLE:
        return "TABLE";
      case VIEW:
        return "VIEW";
      case INDEX:
        return "INDEX";

      default:
        return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getPrimaryKeyColumns(Table table) throws SQLException {
    Map<Short, String> primaryKeys = Maps.newTreeMap();
    ResultSet rs = getMetadata().getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName());
    while (rs.next()) {
      primaryKeys.put(rs.getShort(5), rs.getString(4));
    }
    return Lists.newArrayList(primaryKeys.values());
  }

  /**
   * Set the metadata.
   * @param metadata The metadata to set
   */
  public void setMetadata(DatabaseMetaData metadata) {
    m_metadata = metadata;
  }

  /**
   * Get the metadata.
   * @return Returns the metadata
   */
  public DatabaseMetaData getMetadata() {
    return m_metadata;
  }
}