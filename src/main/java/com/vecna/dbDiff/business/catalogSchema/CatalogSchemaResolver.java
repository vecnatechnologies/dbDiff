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

package com.vecna.dbDiff.business.catalogSchema;

import com.vecna.dbDiff.model.CatalogSchema;

/**
 * Determines catalog/schema from the jdbc driver and the jdbc url.
 * @author ogolberg@vecna.com
 */
public interface CatalogSchemaResolver {
  /**
   * Resolve the catalog/schema
   * @param jdbcDriver driver
   * @param jdbcUrl url
   * @return catalog/schema
   */
  public CatalogSchema resolveCatalogSchema(String jdbcDriver, String jdbcUrl);
}
