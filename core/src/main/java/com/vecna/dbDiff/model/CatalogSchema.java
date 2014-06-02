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

import java.util.Objects;


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
   * @return default catalog/schema.
   */
  public static CatalogSchema defaultCatalogSchema() {
    return new CatalogSchema(DEFAULT_CATALOG, DEFAULT_SCHEMA);
  }

  private final String m_catalog;
  private final String m_schema;

  /**
   * Construct a new {@link CatalogSchema}.
   * @param catalog catalog.
   * @param schema schema.
   */
  public CatalogSchema(String catalog, String schema) {
    m_catalog = catalog;
    m_schema = schema;
  }

  /**
   * @return the catalog.
   */
  public String getCatalog() {
    return m_catalog;
  }

  /**
   * @return the schema.
   */
  public String getSchema() {
    return m_schema;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    } else if (getClass() != obj.getClass()) {
      return false;
    } else {
      CatalogSchema other = (CatalogSchema) obj;
      return Objects.equals(getCatalog(), other.getCatalog()) && Objects.equals(getSchema(), other.getSchema());
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), getCatalog(), getSchema());
  }

  @Override
  public String toString() {
    return "[" + m_catalog + "." + m_schema + "]";
  }
}
