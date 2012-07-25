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

import com.google.common.collect.ImmutableMap;
import com.vecna.dbDiff.business.catalogSchema.CatalogSchemaResolver;
import com.vecna.dbDiff.model.CatalogSchema;

/**
 * This abstract factory wires up the default catalog/schema resolver.
 * @author ogolberg@vecna.com
 */
public class DefaultCatalogSchemaResolverFactory {
  /**
   * Creates the default catalog/schema resolver
   * @return default catalog/schema resolver
   */
  public static CatalogSchemaResolver getCatalogSchemaResolver() {
    return new DelegatingByDriverCatalogSchemaResolverImpl(ImmutableMap.of("org.postgresql.Driver",
                                                                           new SimpleCatalogSchemaResolver(CatalogSchema
                                                                                                           .defaultCatalogSchema()),
                                                                           "net.sourceforge.jtds.jdbc.Driver",
                                                                           new SQLServerCatalogSchemaResolver()));
  }
}
