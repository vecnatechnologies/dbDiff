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

package com.vecna.dbDiff.business.catalogSchema.impl;

import com.vecna.dbDiff.business.catalogSchema.CatalogSchemaResolver;
import com.vecna.dbDiff.model.CatalogSchema;

/**
 * Always returns the same instance of catalog/schema
 * @author ogolberg@vecna.com
 */
class SimpleCatalogSchemaResolver implements CatalogSchemaResolver {
  private final CatalogSchema m_catalogSchema;

  /**
   * Create a {@link SimpleCatalogSchemaResolver}
   * @param catalogSchema the catalog/schema to use
   */
  public SimpleCatalogSchemaResolver(CatalogSchema catalogSchema) {
    m_catalogSchema = catalogSchema;
  }

  /**
   * {@inheritDoc}
   */
  public CatalogSchema resolveCatalogSchema(String jdbcDriver, String jdbcUrl) {
    return m_catalogSchema;
  }
}
