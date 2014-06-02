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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.vecna.dbDiff.jdbc.MetadataFactory;
import com.vecna.dbDiff.jdbc.ThreadLocalMetadataFactory;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.db.ForeignKey;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;
import com.vecna.dbDiff.model.relationalDb.RelationalIndex;
import com.vecna.dbDiff.model.relationalDb.RelationalTable;

/**
 * Tests {@link RelationalDatabaseBuilderImpl}.
 *
 * @author ogolberg@vecna.com
 */
public class RelationalDatabaseBuilderTest extends TestCase {
  private RelationalDatabase getDatabase() throws Exception {
    try (MetadataFactory factory = new ThreadLocalMetadataFactory("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")) {
      try (InputStream stream = getClass().getResourceAsStream("/test-db.sql")) {
        String sql = IOUtils.toString(stream);

        Connection ddlConnection = factory.getMetadata().getConnection();
        ddlConnection.setAutoCommit(true);

        for (String sqlStatement : sql.split(";")) {
          ddlConnection.createStatement().execute(sqlStatement);
        }
      }

      RelationalDatabaseBuilder builder = new RelationalDatabaseBuilderImpl(factory);
      return builder.createRelationalDatabase(new CatalogSchema(null, "PUBLIC"));
    }
  }

  /**
   * Find a unique index that spans a list of columns. Fail if there's no such unique index.
   */
  private RelationalIndex getIndex(RelationalTable table, String ... columnNames) {
    Collection<RelationalIndex> indices = table.getIndicesByColumns().get(Arrays.asList(columnNames));

    assertEquals("cannot find a unique index for columns " + Arrays.asList(columnNames), 1, indices.size());
    return indices.iterator().next();
  }

  /**
   * Find a unique foreign key by name. Fail if there's no such unique foreign key.
   */
  private ForeignKey getForeignKey(RelationalTable table, String name) {
    Collection<ForeignKey> foreignKeys = table.getFksByName(name);
    assertEquals("cannot find a unique foreign key " + name, 1, foreignKeys.size());
    return foreignKeys.iterator().next();
  }

  /**
   * Test {@link RelationalDatabaseBuilderImpl#createRelationalDatabase(CatalogSchema)).
   * @throws Exception
   */
  public void testCreateRelationalDatabase() throws Exception {
    RelationalDatabase database = getDatabase();

    assertEquals("wrong number of tables", 2, database.getTables().size());

    RelationalTable personTable = database.getTableByName("PERSON");
    assertNotNull("table PERSON not found", personTable);

    List<Column> cols = new ArrayList<>(personTable.getColumns());
    assertEquals("wrong number of columns", 3, cols.size());
    assertEquals("wrong column", "ID", cols.get(0).getName());
    assertEquals("wrong column", Boolean.FALSE, cols.get(0).getIsNullable());
    assertEquals("wrong column", Types.BIGINT, cols.get(0).getType());

    assertEquals("wrong column", "NAME", cols.get(1).getName());
    assertEquals("wrong column", Boolean.FALSE, cols.get(1).getIsNullable());
    assertEquals("wrong column", Types.VARCHAR, cols.get(1).getType());

    assertEquals("wrong column", "DOB", cols.get(2).getName());
    assertEquals("wrong column", Boolean.FALSE, cols.get(2).getIsNullable());
    assertEquals("wrong column", Types.TIMESTAMP, cols.get(2).getType());

    assertEquals("wrong number PK columns", Arrays.asList("ID"), personTable.getPkColumns());

    assertEquals("wrong number of indices", 2, personTable.getIndices().size());

    RelationalIndex nameDOB = getIndex(personTable, "NAME", "DOB");
    assertEquals("NAME_DOB_IDX", nameDOB.getName());

    getIndex(personTable, "ID");

    RelationalTable joinTable = database.getTableByName("PERSON_RELATIVES");

    cols = new ArrayList<>(joinTable.getColumns());
    assertEquals("wrong number of columns", 3, cols.size());

    assertEquals("wrong column", "PERSON_ID", cols.get(0).getName());
    assertEquals("wrong column", Boolean.FALSE, cols.get(0).getIsNullable());
    assertEquals("wrong column", Types.BIGINT, cols.get(0).getType());

    assertEquals("wrong column", "RELATIVE_ID", cols.get(1).getName());
    assertEquals("wrong column", Boolean.FALSE, cols.get(1).getIsNullable());
    assertEquals("wrong column", Types.BIGINT, cols.get(1).getType());

    assertEquals("wrong column", "RELATIONSHIP", cols.get(2).getName());
    assertEquals("wrong column", Boolean.TRUE, cols.get(2).getIsNullable());
    assertEquals("wrong column", Types.VARCHAR, cols.get(2).getType());

    assertEquals("wrong PK columns", Arrays.asList("PERSON_ID", "RELATIVE_ID"), joinTable.getPkColumns());

    getIndex(joinTable, "PERSON_ID", "RELATIVE_ID");

    ForeignKey fkPerson = getForeignKey(joinTable, "FK_PERSON");
    assertEquals("PERSON_ID", fkPerson.getFkColumn());
    assertEquals("PERSON_RELATIVES", fkPerson.getFkTable());

    assertEquals("ID", fkPerson.getPkColumn());
    assertEquals("PERSON", fkPerson.getPkTable());

    ForeignKey fkRelative = getForeignKey(joinTable, "FK_RELATIVE");
    assertEquals("RELATIVE_ID", fkRelative.getFkColumn());
    assertEquals("PERSON_RELATIVES", fkRelative.getFkTable());

    assertEquals("ID", fkRelative.getPkColumn());
    assertEquals("PERSON", fkRelative.getPkTable());
  }
}
