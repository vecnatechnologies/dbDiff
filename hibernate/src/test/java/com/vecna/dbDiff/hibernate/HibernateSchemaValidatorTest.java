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
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.vecna.dbDiff.business.dbCompare.impl.RdbCompareError;
import com.vecna.dbDiff.business.dbCompare.impl.RdbDiffEngine;
import com.vecna.dbDiff.business.relationalDb.impl.RelationalDatabaseBeanImpl;
import com.vecna.dbDiff.dao.MetadataDao;
import com.vecna.dbDiff.dao.impl.GenericMetadataDaoImpl;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;
import com.vecna.dbDiff.model.relationalDb.RelationalTable;

/**
 * fixing bug Expected '16/boolean' but got '-7/bool'
 *
 * @author greg.zheng@vecna.com
 */
public class HibernateSchemaValidatorTest {

  private static final String TEST_TABLE = "book";
  private static final String SQLTYPE_CONF = "hibernate-sqltype.cfg.xml";

  /**
   * @throws Exception
   */
  @Test
  public void testValidate() throws Exception {
    Configuration configuration = new Configuration();
    configuration.configure(SQLTYPE_CONF);
    configuration.buildMappings();

    HibernateSchemaValidator validator = new HibernateSchemaValidator(configuration);
    List<RdbCompareError> errors = validator.validate();
    for (RdbCompareError error : errors) {
      System.out.println(error.getMessage());
    }
    assertEquals("Found "+errors.size()+" mismatched columns.", 0, errors.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCompareTables() throws Exception {
    Configuration configuration = new Configuration();
    configuration.configure(SQLTYPE_CONF);
    configuration.buildMappings();
    HibernateMappingsConverter converter = new HibernateMappingsConverter(CatalogSchema.defaultCatalogSchema());
    RelationalDatabase rdb = converter.convert(configuration, configuration.buildMapping());
    List<RelationalTable> tables = Lists.newArrayList(rdb.getTables());

    assertEquals("Expect signle table", 1, tables.size());
    assertEquals(TEST_TABLE, tables.get(0).getTable().getName());

    List<RdbCompareError> errors = new ArrayList<RdbCompareError>();
    RelationalTable testTable = null;
    for (RelationalTable table : tables) {
      if (TEST_TABLE.equalsIgnoreCase(table.getTable().getName())) {
        testTable = table;
        break;
      }
    }

    RelationalDatabase liveSchema;
    Connection conn = null;
    try {
      String jdbcUrl = configuration.getProperty("hibernate.connection.url");
      String jdbcUser= configuration.getProperty("hibernate.connection.username");
      String jdbcPassword = configuration.getProperty("hibernate.connection.password");
      conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
      MetadataDao metadataDao = new GenericMetadataDaoImpl(conn);
      RelationalDatabaseBeanImpl rdbBean = new RelationalDatabaseBeanImpl();
      rdbBean.setMetadataDao(metadataDao);
      CatalogSchema cs = new CatalogSchema(null, "public");
      liveSchema = rdbBean.createRelationalDatabase(cs);
    } finally {
      if (conn != null) {
        conn.close();
      }
    }

    // Check every reference table exists in the test db
    RelationalTable sqlTable = null;
    for (RelationalTable table : liveSchema.getTables()) {
      if (TEST_TABLE.equalsIgnoreCase(table.getTable().getName())) {
        sqlTable = table;
        break;
      }
    }

    //    new RdbDiffEngine().compareColumns(testTable, sqlTable, errors);
    new RdbDiffEngine().compareRelationalTables(testTable, sqlTable, errors);
    for (RdbCompareError error : errors) {
      System.out.println(error.getMessage());
      assertTrue("Expect mismatched boolean type", error.getMessage().endsWith("Expected '16/boolean' but got '-7/bool'"));
    }
    assertEquals("Found mismatched column(s)", 0, errors.size());
  }
}
