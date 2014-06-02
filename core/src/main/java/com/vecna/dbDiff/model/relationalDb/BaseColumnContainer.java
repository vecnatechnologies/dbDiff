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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.db.Column;

/**
 * DB schema item that contains or references a set of columns.
 *
 * @author ogolberg@vecna.com
 */
public abstract class BaseColumnContainer extends NamedSchemaItem {
  /**
   * Create a new instance.
   * @param catalogSchema catalog/schema.
   * @param name name.
   */
  public BaseColumnContainer(CatalogSchema catalogSchema, String name) {
    super(catalogSchema, name);
  }

  /**
   * Create a new instance.
   * @param catalog catalog.
   * @param schema schema.
   * @param name name.
   */
  public BaseColumnContainer(String catalog, String schema, String name) {
    super(catalog, schema, name);
  }

  private Map<String, Column> m_columnsByName = new LinkedHashMap<>(); //An internal search index of cols by name.

  /**
   * Set the columns.
   * @param columns The columns to set
   */
  public void setColumns(List<Column> columns) {
    for (Column c : columns) {
      m_columnsByName.put(c.getName(), c);
    }
  }

  /**
   * @return ordered collection of columns.
   */
  public Collection<Column> getColumns() {
    return m_columnsByName.values();
  }

  /**
   * @return ordered collection of column names.
   */
  public List<String> getColumnNames() {
    List<String> names = Lists.newArrayList();
    for (Column col : m_columnsByName.values()) {
      names.add(col.getName());
    }
    return names;
  }

  /**
   * Get a column by name.
   * @param name name.
   * @return column with the given name (or null).
   */
  public Column getColumnByName(String name) {
    return m_columnsByName.get(name);
  }
}