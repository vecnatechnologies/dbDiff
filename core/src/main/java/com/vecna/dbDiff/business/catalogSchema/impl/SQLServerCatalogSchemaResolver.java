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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vecna.dbDiff.business.catalogSchema.CatalogSchemaResolver;
import com.vecna.dbDiff.model.CatalogSchema;

/**
 * SQL Server catalog/schema resolver. Defaults to dbo/database name.
 * Database name is parsed from the jdbc url.
 * @author ogolberg@vecna.com
 */
class SQLServerCatalogSchemaResolver implements CatalogSchemaResolver {
  /**
   * {@inheritDoc}
   */
  public CatalogSchema resolveCatalogSchema(String jdbcDriver, String jdbcUrl) {
    if (jdbcUrl == null) {
      throw new IllegalArgumentException("jdbc url is not defined");
    }
    Matcher m = Pattern.compile(".*;DatabaseName=(.*?)(;.*)?").matcher(jdbcUrl);
    if (m.matches()) {
      return new CatalogSchema("dbo", m.group(1));
    } else {
      throw new IllegalArgumentException("jdbc url " + jdbcUrl + " doesn't match the SQL server pattern, " +
                                          "cannot determine schema name");
    }
  }
}
