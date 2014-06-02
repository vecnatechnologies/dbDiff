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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.db.ForeignKey;

/**
 * A model of a table possibly containing indexes and foreign keys
 *
 * Note: Currently supports only single-row foreign keys and unique index names (ie from a single schema/catalog)
 * @author dlopuch@vecna.com
 */
public class RelationalTable extends BaseColumnContainer {
  /**
   * Create a new table.
   * @param catalogSchema catalog/schema.
   * @param name table name.
   */
  public RelationalTable(CatalogSchema catalogSchema, String name) {
    super(catalogSchema, name);
  }

  /**
   * Create a new table.
   * @param catalog catalog.
   * @param schema schema.
   * @param name table name.
   */
  public RelationalTable(String catalog, String schema, String name) {
    super(catalog, schema, name);
  }

  private String m_type;
  private String m_typeName;

  private Multimap<List<String>, RelationalIndex> m_indicesByColumns;

  private Set<ForeignKey> m_fks;

  private SetMultimap<String, ForeignKey> m_fksByName; //An internal search index of fk's by name
                                                    //Note that composite keys have the same name
  private SetMultimap<String, ForeignKey> m_fksByTableColumn; //An internal search index of fk's by the "catalog.schema.table.col"
                                                           //being referenced
  private List<String> m_pkColumns;

  /**
   * Set the indices.
   * @param indices The indices to set
   * @throws InconsistentSchemaException If adding an index not recognized by the current table
   */
  public void setIndices(List<RelationalIndex> indices) throws InconsistentSchemaException {
    ImmutableMultimap.Builder<List<String>, RelationalIndex> indexMapBuilder = ImmutableListMultimap.builder();

    if (getName() == null) {
      throw new InconsistentSchemaException("Trying to add indices without setting a table!");
    } else {
      for (RelationalIndex ri : indices) {
        if (!getCatalogSchema().equals(ri.getCatalogSchema())) {
          throw new InconsistentSchemaException("Index " + ri.getName() + " and table " + getName() + " belong to different catalogs or schemas.");
        }

        indexMapBuilder.put(ri.getColumnNames(), ri);
      }
    }
    m_indicesByColumns = indexMapBuilder.build();
  }
  /**
   * Get the indices. Do NOT modify these indices -- doing so will mess up internal search indexes!
   * @return Returns the indices
   */
  public Collection<RelationalIndex> getIndices() {
    return m_indicesByColumns.values();
  }

  /**
   * Get the multimap that maps lists of column names to the indices spanning those columns
   * @return the multimap that maps lists of column names to the indices spanning those columns
   */
  public Multimap<List<String>, RelationalIndex> getIndicesByColumns() {
    return m_indicesByColumns;
  }

  /**
   * Set the fks.
   * @param fks The fks to set
   * @throws InconsistentSchemaException If mismatch between the added fk's and the table
   */
  public void setFks(Set<ForeignKey> fks) throws InconsistentSchemaException {
    m_fks = fks;
    m_fksByName = HashMultimap.create();
    m_fksByTableColumn = HashMultimap.create();

    // Check validity of fk's and then add them to the search index
    for (ForeignKey fk : fks) {
      if (!getCatalogSchema().equals(fk.getFkCatalogSchema()) || !getName().equals(fk.getFkTable())) {
        throw new InconsistentSchemaException("Foreign key " + fk + " does not match table " + getName());
      }

      //The fk is valid.  Add it to the search indices
      m_fksByName.put(fk.getFkName(), fk);

      String key = fk.getPkCatalogSchema().getCatalog() + "." + fk.getPkCatalogSchema().getSchema() + "." + fk.getPkTable() + "." + fk.getPkColumn();
      m_fksByTableColumn.put(key, fk);
    }
  }

  public String getType() {
    return m_type;
  }

  public void setType(String type) {
    m_type = type;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  /**
   * Gets a COPY of the current table's fk.  If you add/remove any, you must set the new collection so that internal search indexes
   * can be reconstructed.
   * @return Returns a copy of the table's fks
   */
  public List<ForeignKey> getFks() {
    return new LinkedList<>(m_fks);
  }

  /**
   * Retrieve a table's foreign keys by a constraint name.
   * @param name A foreign key name
   * @return a set containing matching ForeignKeys
   */
  public Set<ForeignKey> getFksByName(String name) {
    return m_fksByName.get(name);
  }

  /**
   * Get primary key columns
   * @return primary key columns
   */
  public List<String> getPkColumns() {
    return m_pkColumns;
  }

  /**
   * Set primary key columns
   * @param pkColumns the primary key columns to set
   */
  public void setPkColumns(List<String> pkColumns) {
    m_pkColumns = pkColumns;
  }

  /**
   * Retrieve a table's foreign keys by specifying the column the constraint refers to
   * @param catalog The catalog of the referenced table.  Must be exact match (ie no wildcards)
   * @param schema The schema of the referenced table.  Must be exact match (ie no wildcards)
   * @param table The name of the referenced table.  Must be exact match (ie no wildcards)
   * @param column The column being referenced.  Must be exact match (ie no wildcards)
   * @return All foreign keys matching the specified reference.
   */
  public Set<ForeignKey> getFksByReferencedCol(String catalog, String schema, String table, String column) {
    return m_fksByTableColumn.get(catalog + "." + schema + "." + table + "." + column);
  }
}
