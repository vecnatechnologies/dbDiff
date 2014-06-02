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

import com.google.common.base.Objects;
import com.vecna.dbDiff.model.CatalogSchema;


/**
 * A model of a database foreign key
 * @author dlopuch@vecna.com
 */
public class ForeignKey {
  //Constraint name
  private String m_fkName;
  private String m_keySeq;

  //Table and column the FK belongs to, ResultSet 5-8
  private CatalogSchema m_fkCatalogSchema;
  private String m_fkTable;
  private String m_fkColumn;

  //Table and column the FK points to, ResultSet 1-4
  private CatalogSchema m_pkCatalogSchema;
  private String m_pkTable;
  private String m_pkColumn;
  /**
   * Set the fkName.
   * @param fkName The fkName to set
   */
  public void setFkName(String fkName) {
    m_fkName = fkName;
  }
  /**
   * Get the fkName.
   * @return Returns the fkName
   */
  public String getFkName() {
    return m_fkName;
  }
  /**
   * Set the keySeq.
   * @param keySeq The keySeq to set
   */
  public void setKeySeq(String keySeq) {
    m_keySeq = keySeq;
  }
  /**
   * Get the keySeq.
   * @return Returns the keySeq
   */
  public String getKeySeq() {
    return m_keySeq;
  }

  /**
   * Set the fkTable.
   * @param fkTable The fkTable to set
   */
  public void setFkTable(String fkTable) {
    m_fkTable = fkTable;
  }
  /**
   * Get the fkTable.
   * @return Returns the fkTable
   */
  public String getFkTable() {
    return m_fkTable;
  }
  /**
   * Set the fkColumn.
   * @param fkColumn The fkColumn to set
   */
  public void setFkColumn(String fkColumn) {
    m_fkColumn = fkColumn;
  }
  /**
   * Get the fkColumn.
   * @return Returns the fkColumn
   */
  public String getFkColumn() {
    return m_fkColumn;
  }

  /**
   * Set the pkTable.
   * @param pkTable The pkTable to set
   */
  public void setPkTable(String pkTable) {
    m_pkTable = pkTable;
  }
  /**
   * Get the pkTable.
   * @return Returns the pkTable
   */
  public String getPkTable() {
    return m_pkTable;
  }
  /**
   * Set the pkColumn.
   * @param pkColumn The pkColumn to set
   */
  public void setPkColumn(String pkColumn) {
    m_pkColumn = pkColumn;
  }
  /**
   * Get the pkColumn.
   * @return Returns the pkColumn
   */
  public String getPkColumn() {
    return m_pkColumn;
  }

  public CatalogSchema getFkCatalogSchema() {
    return m_fkCatalogSchema;
  }

  public CatalogSchema getPkCatalogSchema() {
    return m_pkCatalogSchema;
  }

  public void setFkCatalogSchema(CatalogSchema fkCatalogSchema) {
    m_fkCatalogSchema = fkCatalogSchema;
  }

  public void setPkCatalogSchema(CatalogSchema pkCatalogSchema) {
    m_pkCatalogSchema = pkCatalogSchema;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(37, m_pkCatalogSchema, m_pkTable, m_pkColumn,
                            m_fkCatalogSchema, m_fkTable, m_fkColumn,
                            m_fkName, m_keySeq);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ForeignKey)) {
      return false;
    }
    ForeignKey other = (ForeignKey)o;
    return equalsReference(other)
           && equalsFrom(other)
           && Objects.equal(m_fkName, other.getFkName())
           && Objects.equal(m_keySeq, other.getKeySeq());
  }

  /**
   * Returns true if this foreign key refers to the same table/column as another foreign key
   * (ie the referenced catalog, schema, table, and column of the two fk's are equal)
   * @param other Another fk to check
   * @return True if the two fk's point to the same column
   */
  public boolean equalsReference(ForeignKey other) {
    return Objects.equal(m_pkCatalogSchema, other.getPkCatalogSchema())
            && Objects.equal(m_pkTable, other.getPkTable())
            && Objects.equal(m_pkColumn, other.getPkColumn());
  }

  /**
   * Returns true if this foreign key is based off of the same table/column as another foreign key
   * (ie the catalog, schema, table, and column that define the fk reference are equal)
   * @param other Another fk to check
   * @return True if the two fk's point from the same column
   */
  public boolean equalsFrom(ForeignKey other) {
    return Objects.equal(m_fkCatalogSchema, other.getFkCatalogSchema())
            && Objects.equal(m_fkTable, other.getFkTable())
            && Objects.equal(m_fkColumn, other.getFkColumn());
  }

  @Override
  public String toString() {
    return getFkName() + "(" + getKeySeq() + "): " + getFkTable() + "(" + getFkColumn() + ")-->" + getPkTable()
    + "(" + getPkColumn() + ")";
  }
}
