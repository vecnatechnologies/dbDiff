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

package com.vecna.dbDiff.model.db;

import java.io.Serializable;

import com.vecna.dbDiff.model.ColumnType;



/**
 * A model of a DB Table's column
 * @author dlopuch@vecna.com
 */
public class Column implements Comparable<Column>, Serializable {

  private static final long serialVersionUID = -1093206125892737605L;

  // Column parent
  private String m_catalog;
  private String m_schema;
  private String m_table;

  // Column properties
  private String m_name;
  private String m_default;
  private Boolean m_isNullable;

  private Integer m_columnSize;

  private Integer m_ordinal;

  private Boolean m_autoIncrement;

  private ColumnType m_columnType;

  /**
   * Set the catalog.
   * @param catalog The catalog to set
   */
  public void setCatalog(String catalog) {
    m_catalog = catalog;
  }
  /**
   * Get the catalog.
   * @return Returns the catalog
   */
  public String getCatalog() {
    return m_catalog;
  }
  /**
   * Set the schema.
   * @param schema The schema to set
   */
  public void setSchema(String schema) {
    m_schema = schema;
  }
  /**
   * Get the schema.
   * @return Returns the schema
   */
  public String getSchema() {
    return m_schema;
  }
  /**
   * Set the table.
   * @param table The table to set
   */
  public void setTable(String table) {
    m_table = table;
  }
  /**
   * Get the table.
   * @return Returns the table
   */
  public String getTable() {
    return m_table;
  }
  /**
   * Set the name.
   * @param name The name to set
   */
  public void setName(String name) {
    m_name = name;
  }
  /**
   * Get the name.
   * @return Returns the name
   */
  public String getName() {
    return m_name;
  }
  /**
   * Set the default.
   * @param defaultVal The default to set
   */
  public void setDefault(String defaultVal) {
    m_default = defaultVal;
  }
  /**
   * Get the default.
   * @return Returns the default
   */
  public String getDefault() {
    return m_default;
  }
  /**
   * Set the isNullable.
   * @param isNullable The isNullable to set
   */
  public void setIsNullable(Boolean isNullable) {
    m_isNullable = isNullable;
  }
  /**
   * Get the isNullable.
   * @return Returns the isNullable
   */
  public Boolean getIsNullable() {
    return m_isNullable;
  }
  /**
   * Set the size of the column.  For char or date types this is the maximum number of characters, for numeric or decimal types
   * this is precision.
   * @param columnSize The columnSize to set
   */
  public void setColumnSize(Integer columnSize) {
    m_columnSize = columnSize;
  }
  /**
   * Get the size of the column.  For char or date types this is the maximum number of characters, for numeric or decimal types
   * this is precision.
   * @return Returns the columnSize
   */
  public Integer getColumnSize() {
    return m_columnSize;
  }
  /**
   * Set the ordinal.
   * @param ordinal The ordinal to set
   */
  public void setOrdinal(Integer ordinal) {
    m_ordinal = ordinal;
  }
  /**
   * Get the ordinal.
   * @return Returns the ordinal
   */
  public Integer getOrdinal() {
    return m_ordinal;
  }

  /**
   * Get the type, corresponds to java.sql.Types
   * @return Returns the type
   */
  public int getType() {
    return m_columnType.getType();
  }

  /**
   * @return the type name (e.g. float4, bigint, varchar(255))
   */
  public String getTypeName() {
    return m_columnType.getTypeCode();
  }

  /**
   * @return the column type object
   */
  public ColumnType getColumnType() {
    return m_columnType;
  }

  /**
   * set the column type
   * @param columnType the value to set
   */
  public void setColumnType(ColumnType columnType) {
    m_columnType = columnType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Column)) {
      return false;
    }
    Column other = (Column)o;
    return getOrdinal().equals(other.getOrdinal()) && getName().equals(other.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int compareTo(Column o) {
    int ordComp = getOrdinal().compareTo(o.getOrdinal());
    if (ordComp == 0) {
      return getName().compareTo(o.getName());
    }
    return ordComp;
  }
}
