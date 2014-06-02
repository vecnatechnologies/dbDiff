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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Serializable collection of RelationalTables, representing a db.
 *
 * Note that this is designed to work wrt to a single schema or catalog!
 *
 * @author dlopuch@vecna.com
 */
public class RelationalDatabase {
  private final Map<String, RelationalTable> m_tablesByName;

  /**
   * Construct a new instance.
   * @param tables ordered collection of tables.
   */
  public RelationalDatabase(Collection<RelationalTable> tables) {
    m_tablesByName = new LinkedHashMap<>(tables.size());
    for (RelationalTable rt : tables) {
      if (m_tablesByName.containsKey(rt.getName())) {
        throw new InconsistentSchemaException("A RelationalDatabase supports only unique table names of tables of the same "
            + "catalog/schema. Non-unique name found: " + rt.getName());
      }
      m_tablesByName.put(rt.getName(), rt);
    }
  }

  /**
   * @return ordered collection of tables in this database.
   */
  public Collection<RelationalTable> getTables() {
    return Collections.unmodifiableCollection(m_tablesByName.values());
  }

  /**
   * Gets a specific table by name.
   * @param tableName the name of the table.
   * @return the table with the given name.
   */
  public RelationalTable getTableByName(String tableName) {
    return m_tablesByName.get(tableName);
  }
}
