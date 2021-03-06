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

import com.vecna.dbDiff.model.CatalogSchema;

/**
 * DB schema item with name, catalog, and schema.
 *
 * @author ogolberg@vecna.com
 */
public abstract class NamedSchemaItem {
  private final CatalogSchema m_catalogSchema;
  private final String m_name;

  /**
   * Create a new instance.
   * @param catalogSchema catalog/schema.
   * @param name name.
   */
  public NamedSchemaItem(CatalogSchema catalogSchema, String name) {
    m_catalogSchema = catalogSchema;
    m_name = name;
  }

  /**
   * Create a new instance.
   * @param catalog catalog.
   * @param schema schema.
   * @param name name.
   */
  public NamedSchemaItem(String catalog, String schema, String name) {
    this(new CatalogSchema(catalog, schema), name);
  }

  /**
   * @return catalog/schema.
   */
  public CatalogSchema getCatalogSchema() {
    return m_catalogSchema;
  }

  /**
   * @return name.
   */
  public String getName() {
    return m_name;
  }
}
