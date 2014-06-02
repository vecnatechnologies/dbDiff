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

package com.vecna.dbDiff.hibernate;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.cfg.AnnotationConfiguration;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.db.ForeignKey;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;
import com.vecna.dbDiff.model.relationalDb.RelationalIndex;
import com.vecna.dbDiff.model.relationalDb.RelationalTable;
import com.vecna.dbDiff.model.relationalDb.InconsistentSchemaException;

/**
 * Unit tests for {@link HibernateMappingsConverter}
 * @author ogolberg@vecna.com
 */
public class HibernateMappingsConverterTest extends TestCase {
  @SuppressWarnings("deprecation")
  private List<RelationalTable> getSortedTables(String configResource) throws InconsistentSchemaException {
    AnnotationConfiguration config = new AnnotationConfiguration();
    config.configure(configResource);
    config.buildMappings();

    RelationalDatabase rdb = new HibernateMappingsConverter(CatalogSchema.defaultCatalogSchema(), config).convert();
    return new ArrayList<>(rdb.getTables());
  }

  /**
   * Verifies the entire schema for a two-class many-to-many model.
   */
  public void testConvertMappings() throws Exception {
    List<RelationalTable> tables = getSortedTables("hibernate.cfg.xml");

    assertEquals(3, tables.size());

    // validate table names
    assertEquals("bar", tables.get(0).getName());
    assertEquals("bar_foo", tables.get(1).getName());
    assertEquals("foo", tables.get(2).getName());

    // validate PKeys
    assertEquals(Lists.newArrayList("id"), Lists.newArrayList(tables.get(0).getPkColumns()));
    assertEquals(Lists.newArrayList("bar_id", "mapkey"), Lists.newArrayList(tables.get(1).getPkColumns()));
    assertEquals(Lists.newArrayList("id"), Lists.newArrayList(tables.get(2).getPkColumns()));

    // validate FKeys
    assertEquals(0, tables.get(0).getFks().size());
    assertEquals(2, tables.get(1).getFks().size());
    assertEquals(0, tables.get(2).getFks().size());

    ForeignKey barFKey = tables.get(1).getFksByName("fk4f54165a912c2f10").iterator().next();
    assertEquals("bar_id", barFKey.getFkColumn());
    assertEquals("bar", barFKey.getPkTable());
    assertEquals("id", barFKey.getPkColumn());

    ForeignKey fooFKey = tables.get(1).getFksByName("fk4f54165abbdc9929").iterator().next();
    assertEquals("foos_id", fooFKey.getFkColumn());
    assertEquals("foo", fooFKey.getPkTable());
    assertEquals("id", fooFKey.getPkColumn());


    // validate indices
    Multimap<List<String>, RelationalIndex> barIndices = tables.get(0).getIndicesByColumns();
    assertEquals(2, barIndices.size());
    Collection<RelationalIndex> pkeyIndex = barIndices.get(Lists.newArrayList("id"));
    assertEquals(1, pkeyIndex.size());
    assertNull(pkeyIndex.iterator().next().getName());

    Collection<RelationalIndex> uniqueIndex = barIndices.get(Lists.newArrayList("name", "idx"));
    assertEquals(1, uniqueIndex.size());
    assertNull(uniqueIndex.iterator().next().getName());

    Multimap<List<String>, RelationalIndex> barFooIndices = tables.get(1).getIndicesByColumns();
    assertEquals(1, barFooIndices.size());
    pkeyIndex = barFooIndices.get(Lists.newArrayList("bar_id", "mapkey"));
    assertEquals(1, pkeyIndex.size());
    assertNull(pkeyIndex.iterator().next().getName());

    Multimap<List<String>, RelationalIndex> fooIndices = tables.get(2).getIndicesByColumns();
    assertEquals(3, fooIndices.size());
    pkeyIndex = fooIndices.get(Lists.newArrayList("id"));
    assertEquals(1, pkeyIndex.size());
    assertNull(pkeyIndex.iterator().next().getName());
    uniqueIndex = fooIndices.get(Lists.newArrayList("name"));
    assertEquals(1, uniqueIndex.size());
    assertNull(uniqueIndex.iterator().next().getName());
    Collection<RelationalIndex> actualIndex = fooIndices.get(Lists.newArrayList("name", "time"));
    assertEquals(1, actualIndex.size());
    assertEquals("foo_idx_name_time", actualIndex.iterator().next().getName());

    // validate columns
    List<Column> barColumns = Lists.newArrayList(tables.get(0).getColumns());
    Collections.sort(barColumns);
    assertEquals(3, barColumns.size());
    assertEquals("id", barColumns.get(0).getName());
    assertEquals(Types.BIGINT, barColumns.get(0).getType());
    assertEquals(Boolean.FALSE, barColumns.get(0).getIsNullable());
    assertEquals("idx", barColumns.get(1).getName());
    assertEquals(Types.INTEGER, barColumns.get(1).getType());
    assertEquals(Boolean.FALSE, barColumns.get(0).getIsNullable());
    assertEquals("name", barColumns.get(2).getName());
    assertEquals(Types.VARCHAR, barColumns.get(2).getType());
    assertEquals(Boolean.TRUE, barColumns.get(2).getIsNullable());

    List<Column> barFooColumns = Lists.newArrayList(tables.get(1).getColumns());
    Collections.sort(barFooColumns);
    assertEquals(3, barFooColumns.size());
    assertEquals("bar_id", barFooColumns.get(0).getName());
    assertEquals(Types.BIGINT, barFooColumns.get(0).getType());
    assertEquals(Boolean.FALSE, barFooColumns.get(0).getIsNullable());
    assertEquals("foos_id", barFooColumns.get(1).getName());
    assertEquals(Types.BIGINT, barFooColumns.get(1).getType());
    assertEquals(Boolean.FALSE, barFooColumns.get(1).getIsNullable());
    assertEquals("mapkey", barFooColumns.get(2).getName());
    assertEquals(Types.VARCHAR, barFooColumns.get(2).getType());
    assertEquals(Boolean.FALSE, barFooColumns.get(2).getIsNullable());

    List<Column> fooColumns = Lists.newArrayList(tables.get(2).getColumns());
    Collections.sort(fooColumns);
    assertEquals(3, fooColumns.size());
    assertEquals("id", fooColumns.get(0).getName());
    assertEquals(Types.BIGINT, fooColumns.get(0).getType());
    assertEquals(Boolean.FALSE, fooColumns.get(0).getIsNullable());
    assertEquals("name", fooColumns.get(1).getName());
    assertEquals(Types.VARCHAR, fooColumns.get(1).getType());
    assertEquals(Boolean.FALSE, fooColumns.get(1).getIsNullable());
    assertEquals("time", fooColumns.get(2).getName());
    assertEquals(Types.TIMESTAMP, fooColumns.get(2).getType());
    assertEquals(Boolean.TRUE, fooColumns.get(2).getIsNullable());
  }

  /**
   * Verifies that the reported character-type discriminator length is correct
   */
  public void testCharDiscriminatorLength() throws Exception {
    List<RelationalTable> tables = getSortedTables("hibernate-discriminator.cfg.xml");

    Column discriminator = tables.get(0).getColumnByName("dtype");
    assertEquals("character type discriminator should have size 1", Integer.valueOf(1), discriminator.getColumnSize());
  }

  /**
   * Verifies that length is not set for varbinary columns
   */
  public void testVarbinaryLength() throws Exception {
    assertNull("column size shouldn't be set for varbinary columns",
               getSortedTables("hibernate-binary.cfg.xml").get(0).getColumnByName("data").getColumnSize());
  }

  /**
   * Tests Postgres-specific conversion (boolean SQL type and long table/column names)
   */
  public void testPostgresSpecificConversion() throws Exception {
    List<RelationalTable> tables = getSortedTables("hibernate-postgres-quirks.cfg.xml");

    RelationalTable lcTable = tables.get(0);

    assertNotNull("long column names must be truncated to 63 characters",
                  lcTable.getColumnByName("the_name_of_this_column_is_very_very_long_for_no_reason_whatsoe"));

    Column booleanColumn = lcTable.getColumnByName("active");

    assertNotNull("incorrect schema - cannot find the 'active' column", booleanColumn);
    assertEquals("wrong boolean column type", -7, booleanColumn.getType());
    assertEquals("wrong boolean column name", "bool", booleanColumn.getTypeName());

    RelationalTable ltTable = tables.get(1);

    assertEquals("long table names must be truncated to 63 characters",
                 "the_name_of_this_table_is_very_very_long_for_no_reason_whatsoev",
                 ltTable.getName());

  }
}
