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

package com.vecna.dbDiff.model.relationalDb;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.db.Table;

/**
 * A relational table model which contains appropriate columns and indices
 * @author dlopuch@vecna.com
 */
public class RelationalIndex implements Comparable<RelationalIndex>, Serializable {

  private static final long serialVersionUID = 721682300009495950L;

  /** The Table for this RelationalIndex and subclasses */
  protected Table m_table;

  private Map<String, Column> m_columnsByName = Maps.newLinkedHashMap(); //An internal search index of cols by name.

  /**
   * Set the table.
   * @param table The table to set
   */
  public void setTable(Table table) {
    m_table = table;
  }
  /**
   * Get the table.
   * @return Returns the table
   */
  public Table getTable() {
    return m_table;
  }
  /**
   * Set the columns.
   * @param columns The columns to set
   * @throws RelationalValidationException If mismatch between the added columns and the current table
   */
  public void setColumns(List<Column> columns) throws RelationalValidationException {
    if (m_table == null) {
      throw new RelationalValidationException("Adding columns before a table is defined!");
    } else {
      for (Column c : columns) {
// commented out because it only works for tables, not indices
//        if ((c.getCatalog() != null && !c.getCatalog().equals(m_table.getCatalog()))
//            || (c.getSchema() != null && !c.getSchema().equals(m_table.getSchema()))
//            || (c.getTable() != null && !c.getTable().equals(m_table.getName())) ) {
//          throw new RelationalValidationException("Trying to add a column which doesn't match this table:" + c);
//        }
        m_columnsByName.put(c.getName(), c);
      }
    }
  }
  /**
   * Get the columns. Do NOT modify these columns -- doing so will mess up internal search indexes!
   * @return Returns the columns
   */
  public Collection<Column> getColumns() {
    return m_columnsByName.values();
  }

  /**
   * Get the names of this index's columns
   * @return column names
   */
  public List<String> getColumnNames() {
    List<String> names = Lists.newArrayList();
    for (Column col : m_columnsByName.values()) {
      names.add(col.getName());
    }
    return names;
  }

  /**
   * Gets a Column contained in this RelationalIndex by name.
   * @param name Name of the Column
   * @return The Column or null if not found
   */
  public Column getColumnByName(String name) {
    return m_columnsByName.get(name);
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals(Object o) {
    if (!(o instanceof RelationalIndex)) {
      return false;
    }
    RelationalIndex other = (RelationalIndex)o;
    return m_table.equals(other.getTable());
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(RelationalIndex o) {
    return m_table.compareTo(o.getTable());
  }
}
