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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A Serializable collection of RelationalTables, representing a db.
 *
 * Note that this is designed to work wrt to a single schema or catalog!
 *
 * @author dlopuch@vecna.com
 */
public class RelationalDatabase implements Serializable {

  private static final long serialVersionUID = -85351066652741366L;

  private Map<String, RelationalTable> m_tablesByName;

  /**
   * Set the tables.
   * @param tables The tables to set.  All tables must have unique names or be of the same schema/catalog
   * @throws RelationalValidationException If non-unique table name detected
   */
  public void setTables(List<RelationalTable> tables) throws RelationalValidationException {
    m_tablesByName = Maps.newLinkedHashMap();
    for (RelationalTable rt : tables) {
      if (m_tablesByName.containsKey(rt.getTable().getName())) {
        throw new RelationalValidationException("A RelationalDatabase supports only unique table names of tables of the same " +
        		"catalog/schema. Non-unique name found: " + rt.getTable().getName());
      }
      m_tablesByName.put(rt.getTable().getName(), rt);
    }
  }

  /**
   * Get the tables. Do NOT modify these tables -- doing so will mess up internal search indexes!
   * @return Returns the tables.
   */
  public Collection<RelationalTable> getTables() {
    return Collections.unmodifiableCollection(m_tablesByName.values());
  }

  /**
   * Gets a specific relationalTable by name.
   * @param tableName the Name of the table
   * @return the RelationalTable
   */
  public RelationalTable getTableByName(String tableName) {
    return m_tablesByName.get(tableName);
  }
}
