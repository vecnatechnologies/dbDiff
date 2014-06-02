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

package com.vecna.dbDiff.builder;

import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.relationalDb.InconsistentSchemaException;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;


/**
 * Builds a {@link RelationalDatabase} model from a live database.
 *
 * @author dlopuch@vecna.com
 * @author ogolberg@vecna.com
 */
public interface RelationalDatabaseBuilder {
  /**
   * @param catalogSchema The schema to create a RelationalDatabase for.  Note: must have either catalog or schema defined
   * @return A populated RelationalDatabase object
   * @throws RelationalDatabaseReadException if database communication failed.
   * @throws InconsistentSchemaException if schema information was inconsistent (see {@link InconsistentSchemaException}).
   */
  RelationalDatabase createRelationalDatabase(CatalogSchema catalogSchema) throws RelationalDatabaseReadException, InconsistentSchemaException;
}
