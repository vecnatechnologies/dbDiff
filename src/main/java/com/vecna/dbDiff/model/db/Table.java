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
import java.util.Set;



/**
 * A model of a DB Table
 * @author dlopuch@vecna.com
 */
public class Table implements Comparable<Table>, Serializable {
  
  private static final long serialVersionUID = 7278038316870729732L;
  
  private String m_catalog;
  private String m_schema;
  
  private String m_name;
  private String m_type;
  private String m_typeName;
  
  private Set<String> m_indexNames; 

  /**
   * Set the table's name.
   * @param name The tableName to set
   */
  public void setName(String name) {
    m_name = name;
  }

  /**
   * Get the table's name.
   * @return Returns the tableName
   */
  public String getName() {
    return m_name;
  }
  
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
   * {@inheritDoc}
   */
  public boolean equals(Object o) {
    if (!(o instanceof Column)) {
      return false;
    }
    Table other = (Table)o;
    return m_name.equals(other.getName());
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(Table o) {
    return m_name.compareTo(o.getName());
  }

  /**
   * Set the type.
   * @param type The type to set
   */
  public void setType(String type) {
    m_type = type;
  }

  /**
   * Get the type.
   * @return Returns the type
   */
  public String getType() {
    return m_type;
  }

  /**
   * Set the typeName.
   * @param typeName The typeName to set
   */
  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  /**
   * Get the typeName.
   * @return Returns the typeName
   */
  public String getTypeName() {
    return m_typeName;
  }

  /**
   * Set the names of indices part of this table, result of the getIndexInfo call on this table.
   * Index names should refer to valid tables in the same catalog/schema but of type INDEX.
   * @param indexNames The indexNames to set
   */
  public void setIndexNames(Set<String> indexNames) {
    m_indexNames = indexNames;
  }

  /**
   * Get the names of indices part of this table, result of the getIndexInfo call on this table
   * Index names should refer to valid tables in the same catalog/schema but of type INDEX.
   * @return Returns the indexNames
   */
  public Set<String> getIndexNames() {
    return m_indexNames;
  }

  
}
