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

package com.vecna.dbDiff.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.vecna.dbDiff.business.dbCompare.impl.RdbDiffEngine;
import com.vecna.dbDiff.business.relationalDb.impl.RelationalDatabaseBeanImpl;
import com.vecna.dbDiff.dao.MetadataDao;
import com.vecna.dbDiff.dao.impl.GenericMetadataDaoImpl;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.db.ForeignKey;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;
import com.vecna.dbDiff.model.relationalDb.RelationalIndex;
import com.vecna.dbDiff.model.relationalDb.RelationalTable;

/**
 * A quick-and-dirty db initilization and dependency builder class for all tools
 * @author dlopuch@vecna.com
 */
public class ToolsCommon {
  
  protected static final String DEFAULT_DB_FILENAME = "myDb.ser";
  protected static final String DEFAULT_JDBC_DRIVER_CLASS = "org.postgresql.Driver";
  
  //Database
  /** Database Connection to use */
  protected Connection m_dbConnection;
  
  //Daos
  /** Metadata Dao*/
  protected MetadataDao m_metadataDao;
  
  //Beans
  /** RelationalDatabaseBean */
  protected RelationalDatabaseBeanImpl m_relationalDatabaseBean;
  protected RdbDiffEngine m_rdbDiffEngine;
  
  
  /**
   * Makes and initialized the connection to the database
   * @param jdbcDriver a JDBC driver name in the project path, ie "org.postgresql.Driver" 
   * @param url A JDBC db URL, specific to the driver used, ie "jdbc:postgresql://localhost/my_db_name"
   * @param user A username
   * @param pass A password
   */
  protected void makeConnection(String jdbcDriver, String url, String user, String pass)  {
    // Load the driver
    try {
      Class.forName(jdbcDriver);
    } catch(Exception e) {
      System.out.println("Unable to load the specified JDBC driver class: " + e);
    } finally {
      // Connect
      Connection dbCon = null;
      try {
        dbCon = DriverManager.getConnection(url, user, pass);
      } catch (SQLException e) {
        System.out.println("Error making db connection: " + e);
      } finally {
        if (dbCon == null) {
          throw new RuntimeException("Unable to make db connection. Check login credentials");
        }
        m_dbConnection = dbCon;
      }
    }
  }
  
  /**
   * Close the tool's database connection, if active.
   */
  protected void closeConnection() {
    if (m_dbConnection != null) {
      try {
        m_dbConnection.close();
      } catch (SQLException e){
        //do nothing
      }
    }
  }
  
  /**
   * Sets up and configures all Daos, Beans, and connections.  Must be called AFTER connection has been established.
   * Method has lofty dreams of one day being replaced by Spring injection
   */
  protected void initDependencies() {
    if (m_dbConnection == null) {
      throw new RuntimeException("Must make a dbConnection before initializing stuff.");
    }
    m_metadataDao = new GenericMetadataDaoImpl(m_dbConnection);
    
    m_relationalDatabaseBean = new RelationalDatabaseBeanImpl();
    m_relationalDatabaseBean.setMetadataDao(m_metadataDao);
    
    m_rdbDiffEngine = new RdbDiffEngine();
  }

  /**
   * Parse a main method's args
   * @param args the args array
   * @param url String to put the url param into
   * @param username String to put the username param into
   * @param pw String to put the password param into
   * @param filename String to put the filename param into
   * @param jdbcDriver String to put the jdbcDriver param into
   */
  protected static void parseArgs(String[] args, StringBuilder url, StringBuilder username, StringBuilder pw, 
                                  StringBuilder filename, StringBuilder jdbcDriver) {
    for (int i=0; i<args.length; i++) {
      if ("-l".equals(args[i]) || "--url".equals(args[i])) {
        url.setLength(0);
        url.append(args[++i]);
      } else if ("-u".equals(args[i]) || "--username".equals(args[i])) {
        username.setLength(0);
        username.append(args[++i]);
      } else if ("-p".equals(args[i]) || "--pw".equals(args[i])) {
        pw.setLength(0);
        pw.append(args[++i]);
      } else if ("-f".equals(args[i]) || "--filename".equals(args[i])) {
        filename.setLength(0);
        filename.append(args[++i]);
      } else if ("-j".equals(args[i]) || "--jdbc".equals(args[i])) {
        jdbcDriver.setLength(0);
        jdbcDriver.append(args[++i]);
      }
    }
  }
  
  /**
   * Debug method to print a representation of a RelationalDatabase object to stdout
   * @param rdb a RelationalDatabase object to print
   * @throws Exception if problems
   */
  protected void printRelationalDatabase(RelationalDatabase rdb) throws Exception {
    int numTables = 0;
    int numIndexes = 0;
    int numIndexedCols = 0;
    int numColumns = 0;
    int numFks = 0;
    
    System.out.println(rdb.getTables().size() + " Tables Found!");
    
    for (RelationalTable t : rdb.getTables()) {
      numTables++;
      System.out.println("");
      System.out.println(t.getTable().getType() + ": " + t.getTable().getName());
      
      //Table columns
      System.out.println("  + COLS:");
      for (Column c : t.getColumns()) {
        numColumns++;
        System.out.println("    - " + c.getOrdinal() + ") " + c.getName() 
                           + (c.getIsNullable() ? "" : "   NOT NULL")
                           + (StringUtils.isNotBlank(c.getDefault()) ? "   DEFAULT: '" + c.getDefault() + "'" : ""));
      }
      
      //FK's
      if (CollectionUtils.isNotEmpty(t.getFks())) {
        System.out.println("  + FK's:");
        for (ForeignKey fk : t.getFks()) {
          numFks++;
          System.out.println("     - " + fk.getFkName() + ": (" + fk.getFkColumn() + ") -->  "
                                 + fk.getPkTable() + "(" + fk.getPkColumn() +")");
        }
      }
      
      if (CollectionUtils.isNotEmpty(t.getIndices())) {
        System.out.println("   + INDEXES:");
        for (RelationalIndex i : t.getIndices()) {
          numIndexes++;
          System.out.println("     + " + i.getTable().getName());
          for (Column c : i.getColumns()) {
            numIndexedCols++;
            System.out.println("       - " + c.getOrdinal() + ") " + c.getName());
          }
        }
      }
    }
    
    System.out.println();
    System.out.println("SUMMARY:");
    System.out.println("  - number of tables:\t\t" + numTables);
    System.out.println("  - number of columns:\t\t" + numColumns);
    System.out.println("  - number of foreignKeys:\t" + numFks);
    System.out.println("  - number of indexes:\t\t" + numIndexes);
    System.out.println("  - number of indexed columns:\t" + numIndexedCols);
  }
}
