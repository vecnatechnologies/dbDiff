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

package com.vecna.dbDiff.model;


/**
 * Encapsulation for the current catalog/schema of interest.
 * @author dlopuch@vecna.com
 */
public class CatalogSchema {
  /**
   * Default schema
   */
  public static final String DEFAULT_SCHEMA = "public";

  /**
   * Default catalog
   */
  public static final String DEFAULT_CATALOG = null;

  /**
   * Default catalog/schema combination
   */
  public static CatalogSchema defaultCatalogSchema() {
    return new CatalogSchema(DEFAULT_CATALOG, DEFAULT_SCHEMA);
  }

  private String m_catalog;
  private String m_schema;

  /**
   * Convenience constructor
   * @param catalog
   * @param schema
   */
  public CatalogSchema(String catalog, String schema) {
    setCatalog(catalog);
    setSchema(schema);
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
}
