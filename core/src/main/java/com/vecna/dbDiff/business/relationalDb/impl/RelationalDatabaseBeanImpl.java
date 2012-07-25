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

package com.vecna.dbDiff.business.relationalDb.impl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.vecna.dbDiff.dao.MetadataDao;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.TableType;
import com.vecna.dbDiff.model.db.Table;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;
import com.vecna.dbDiff.model.relationalDb.RelationalIndex;
import com.vecna.dbDiff.model.relationalDb.RelationalTable;
import com.vecna.dbDiff.model.relationalDb.RelationalValidationException;

/**
 * Populates a RelationalDatabase model
 * @author dlopuch@vecna.com
 */
public class RelationalDatabaseBeanImpl {

  private MetadataDao m_metadataDao;

  /**
   * @param catalogSchema The schema to create a RelationalDatabase for.  Note: must have either catalog or schema defined
   * @return A populated RelationalDatabase object
   * @throws SQLException if problem reading metadata
   * @throws RelationalValidationException If bad or unsupported relational constraints
   */
  public RelationalDatabase createRelationalDatabase(CatalogSchema catalogSchema)
    throws SQLException, RelationalValidationException {
    RelationalDatabase rdb = new RelationalDatabase();

    //First grab all the applicable indices.  Map them according to their names
    Map<String, Table> allIndices = new HashMap<String, Table>();
    for (Table i : m_metadataDao.getTables(catalogSchema, TableType.INDEX)) {
      if (allIndices.containsKey(i.getName())) {
        throw new RelationalValidationException("Indexes with duplicate name exists in the specified catalog and schema! Name is: "
                                                + i.getName());
      }
      allIndices.put(i.getName(), i);
    }

    //Grab all the tables
    Set<Table> tables = m_metadataDao.getTables(catalogSchema, TableType.TABLE);
    List<Table> sortedTs = new ArrayList<Table>(tables);
    Collections.sort(sortedTs);

    //Now build the relational tables
    List<RelationalTable> rts = new LinkedList<RelationalTable>();
    for (Table t : sortedTs) {
      RelationalTable rt = new RelationalTable();

      //Table and columns
      rt.setTable(t);
      rt.setColumns(m_metadataDao.getColumns(t));

      //Foreign Keys
      rt.setFks(m_metadataDao.getForeignKeys(t));

      //Primary key
      rt.setPkColumns(m_metadataDao.getPrimaryKeyColumns(t));

      //Resolve the indices
      List<RelationalIndex> tIndices = new LinkedList<RelationalIndex>();
      if (t.getIndexNames() != null) {
        for (String iName : t.getIndexNames()) {
          if (!allIndices.containsKey(iName) || allIndices.get(iName) == null) {
            throw new RelationalValidationException("Table '" + t.getName() + "' specified an unknown Index '" + iName + "'!");
          }
          RelationalIndex ri = new RelationalIndex();
          ri.setTable(allIndices.get(iName));
          ri.setColumns(m_metadataDao.getColumns(ri.getTable()));
          tIndices.add(ri);
        }
      }
      rt.setIndices(tIndices);

      rts.add(rt);
    }

    rdb.setTables(rts);
    return rdb;
  }

  /**
   * Serialized a RelationalDatabase object to the specified filename
   * @param rdb A relationalDatabase object to serialize
   * @param filename Filename to serialize to
   * @throws IOException If error serializing
   */
  public void serializeRelationalDatabase(RelationalDatabase rdb, String filename) throws IOException {
    if (StringUtils.isBlank(filename)) {
      filename = "myDb.ser";
    }

    FileOutputStream fos = new FileOutputStream(filename);
    ObjectOutputStream out = new ObjectOutputStream(fos);

    out.writeObject(rdb);
    out.close();
  }

  /**
   * @param filename
   * @return a deserialized RelationalDatabase
   * @throws IOException If error deserializing it
   * @throws ClassNotFoundException If problem deserializing it
   */
  public RelationalDatabase deserializeRelationalDatabase(String filename) throws IOException, ClassNotFoundException {
    if (StringUtils.isBlank(filename)) {
      filename = "myDb.ser";
    }

    FileInputStream fis = new FileInputStream(filename);
    ObjectInputStream in = new ObjectInputStream(fis);
    RelationalDatabase rdb = (RelationalDatabase)in.readObject();

    in.close();

    return rdb;
  }


  /**
   * Set the metadataDao.
   * @param metadataDao The metadataDao to set
   */
  public void setMetadataDao(MetadataDao metadataDao) {
    m_metadataDao = metadataDao;
  }

  /**
   * Get the metadataDao.
   * @return Returns the metadataDao
   */
  public MetadataDao getMetadataDao() {
    return m_metadataDao;
  }


}
