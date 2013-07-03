/**
 * NOTICE: This software  source code and any of  its derivatives are the
 * confidential  and  proprietary   information  of  Vecna  Technologies,
 * Inc. (such source  and its derivatives are hereinafter  referred to as
 * "Confidential Information"). The  Confidential Information is intended
 * to be  used exclusively by  individuals or entities that  have entered
 * into either  a non-disclosure agreement or license  agreement (or both
 * of  these agreements,  if  applicable) with  Vecna Technologies,  Inc.
 * ("Vecna")   regarding  the  use   of  the   Confidential  Information.
 * Furthermore,  the  Confidential  Information  shall be  used  only  in
 * accordance  with   the  terms   of  such  license   or  non-disclosure
 * agreements.   All  parties using  the  Confidential Information  shall
 * verify that their  intended use of the Confidential  Information is in
 * compliance  with and  not in  violation of  any applicable  license or
 * non-disclosure  agreements.  Unless expressly  authorized by  Vecna in
 * writing, the Confidential Information  shall not be printed, retained,
 * copied, or  otherwise disseminated,  in part or  whole.  Additionally,
 * any party using the Confidential  Information shall be held liable for
 * any and  all damages incurred  by Vecna due  to any disclosure  of the
 * Confidential  Information (including  accidental disclosure).   In the
 * event that  the applicable  non-disclosure or license  agreements with
 * Vecna  have  expired, or  if  none  currently  exists, all  copies  of
 * Confidential Information in your  possession, whether in electronic or
 * printed  form, shall be  destroyed or  returned to  Vecna immediately.
 * Vecna  makes no  representations  or warranties  hereby regarding  the
 * suitability  of  the   Confidential  Information,  either  express  or
 * implied,  including  but not  limited  to  the  implied warranties  of
 * merchantability,    fitness    for    a   particular    purpose,    or
 * non-infringement. Vecna  shall not be liable for  any damages suffered
 * by  licensee as  a result  of  using, modifying  or distributing  this
 * Confidential Information.  Please email [info@vecnatech.com]  with any
 * questions regarding the use of the Confidential Information.
 */

package com.vecna.dbDiff.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;
import com.vecna.dbDiff.model.relationalDb.RelationalTable;

/**
 * This class tests Hibernate type mapping to an SQL type, implemented in PostgreSqlTypeMapper
 * @author greg.zheng@vecna.com
 */
public class PostgreSqlTypeMapperTest {
  private static final String SQLTYPE_CONF = "hibernate-sqltype.cfg.xml";
  private static final String SQLTYPE_CONF_NEG = "hibernate-sqltype-negtive.cfg.xml";

  /**
   * @throws Exception
   */
  @Test
  public void testValidSqlTypeMapper() throws Exception {
    Configuration configuration = new Configuration();
    configuration.configure(SQLTYPE_CONF);
    configuration.buildMappings();
    final HibernateMappingsConverter converter = new HibernateMappingsConverter(CatalogSchema.defaultCatalogSchema());
    final RelationalDatabase rdb = converter.convert(configuration, configuration.buildMapping());
    final RelationalTable table = applySqlTypeMapping(rdb);
    for (Column c : table.getColumns()) {
      if (StringUtils.equalsIgnoreCase(c.getTypeName(), "bool")) {
        assertEquals("Expect -7 as a column datatype for bool.", -7, c.getType());
      }
      if (StringUtils.equalsIgnoreCase(c.getTypeName(), "boolean")) {
        fail("Expect bool, but found boolean.");
      }
    }
  }

  /**
   * Negative test case - no HibernateSqlTypeMapper is defined.
   * <property name="hibernate.sqltype.mapper"></property>
   * or
   * <property name="hibernate.sqltype.mapper">   </property>
   * or does not exist at all
   * @throws Exception
   */
  @Test
  public void testMissingSqlTypeMapper() throws Exception {
    Configuration configuration_negtive  = new Configuration();
    configuration_negtive.configure(SQLTYPE_CONF_NEG);
    configuration_negtive.buildMappings();

    final HibernateMappingsConverter converter = new HibernateMappingsConverter(CatalogSchema.defaultCatalogSchema());
    final RelationalDatabase rdb = converter.convert(configuration_negtive, configuration_negtive.buildMapping());
    applySqlTypeMapping(rdb);
    final RelationalTable table = applySqlTypeMapping(rdb);
    for (Column c : table.getColumns()) {
      if (StringUtils.equalsIgnoreCase(c.getTypeName(), "bool")) {
        fail("Expect boolean, but found bool.");
      }
      if (StringUtils.equalsIgnoreCase(c.getTypeName(), "boolean")) {
        assertEquals("Expect 16 as a column datatype for boolean.", 16, c.getType());
      }
    }
  }

  private RelationalTable applySqlTypeMapping(final RelationalDatabase rdb) throws Exception {
    final List<RelationalTable> tables = Lists.newArrayList(rdb.getTables());
    Collections.sort(tables);
    assertEquals("Expect one data model in the config file.", 1, tables.size());
    assertEquals("Expect only one table is configured", 1, tables.size());
    RelationalTable table = tables.get(0);
    validateTable(table);
    return table;
  }

  private void validateTable(RelationalTable table) {
    for (Column c : table.getColumns()) {
      if (StringUtils.equalsIgnoreCase(c.getTypeName(), "bigint")) {
        assertEquals("Expect -5 as a column datatype for integer.", -5, c.getType());
      }
      if (StringUtils.equalsIgnoreCase(c.getTypeName(), "varchar")) {
        assertEquals("Expect 12 as a column datatype for varchar.", 12, c.getType());
      }
      if (StringUtils.equalsIgnoreCase(c.getTypeName(), "bool")) {
        assertEquals("Expect -7 as a column datatype for bool.", -7, c.getType());
      }
      if (StringUtils.equalsIgnoreCase(c.getTypeName(), "boolean")) {
        assertEquals("Expect 16 as a column datatype for boolean.", 16, c.getType());
      }
    }
  }

}
