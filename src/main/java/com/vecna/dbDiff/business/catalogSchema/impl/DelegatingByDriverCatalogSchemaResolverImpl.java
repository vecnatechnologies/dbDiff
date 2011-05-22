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

import java.util.Map;

import com.vecna.dbDiff.business.catalogSchema.CatalogSchemaResolver;
import com.vecna.dbDiff.model.CatalogSchema;

/**
 * Delegates to another {@link CatalogSchemaResolver} based on the jdbc driver (i.e. the db type).
 * @author ogolberg@vecna.com
 */
class DelegatingByDriverCatalogSchemaResolverImpl implements CatalogSchemaResolver {
  private final Map<String, CatalogSchemaResolver> m_resolverMap;

  /**
   * {@inheritDoc}
   */
  public CatalogSchema resolveCatalogSchema(String jdbcDriver, String jdbcUrl) {
    if (jdbcDriver == null) {
      throw new IllegalArgumentException("need to know the jdbc driver to determine catalog/schema");
    }
    CatalogSchemaResolver resolver = m_resolverMap.get(jdbcDriver);
    if (resolver == null) {
      throw new IllegalArgumentException("driver " + jdbcDriver + " is not supported");
    } else {
      return resolver.resolveCatalogSchema(jdbcDriver, jdbcUrl);
    }
  }

  /**
   * Create a new {@link DelegatingByDriverCatalogSchemaResolverImpl}
   * @param resolverMap resolver map
   */
  public DelegatingByDriverCatalogSchemaResolverImpl(Map<String, CatalogSchemaResolver> resolverMap) {
    m_resolverMap = resolverMap;
  }
}
