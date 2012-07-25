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

import org.apache.commons.lang.StringUtils;

import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;

/**
 * @author dlopuch@vecna.com
 */
public class MakeReferenceDatabase extends ToolsCommon {


  private void run(String url, String username, String pw, String filename, String jdbcDriver) throws Exception {
    makeConnection(jdbcDriver, url, username, pw);
    initDependencies();

    //Make the rdb
    System.out.print("Reading and analyzing reference db...");
    CatalogSchema cs = CatalogSchema.defaultCatalogSchema();
    RelationalDatabase rdb = m_relationalDatabaseBean.createRelationalDatabase(cs);
    closeConnection();
    System.out.println("SUCCESS!");

    //Serialize it
    System.out.println();
    System.out.print("Outputting analyzed db to file: \"" + filename + "\"...");
    m_relationalDatabaseBean.serializeRelationalDatabase(rdb, filename);
    System.out.println("SUCCESS!");

    //Display It
    System.out.println();
    System.out.println();
    printRelationalDatabase(rdb);
  }

  /**
   * @param args
   *  -l, --url: A jdbc URL, ie "jdbc:postgresql://localhost/my_db_name"
   *  -u, --username: username for the database
   *  -p, --pw:  pw for the database
   *  -f, --filename:  filename of reference db (default: "myDb.ser")
   *  -j, --jdbc:  jdbc driver classname in build path (default: "org.postgresql.Driver")
   * @throws Exception If some kind of error
   */
  public static void main(String[] args) throws Exception {
    //Params
    StringBuilder url = new StringBuilder();
    StringBuilder username = new StringBuilder();
    StringBuilder pw = new StringBuilder();

    StringBuilder referenceDbFile = new StringBuilder(DEFAULT_DB_FILENAME);
    StringBuilder jdbcDriver = new StringBuilder(DEFAULT_JDBC_DRIVER_CLASS);

    parseArgs(args, url, username, pw, referenceDbFile, jdbcDriver);

    if (StringUtils.isBlank(url.toString())) {
      System.out.println("[ERROR] JDBC url required, eg \"jdbc:postgresql://localhost/sqm\".  Use --url or -l param");
    } else if (StringUtils.isBlank(username.toString())) {
      System.out.println("[ERROR] JDBC username required, eg \"admin\".  Use --username or -u param");
    } else {
      MakeReferenceDatabase me = new MakeReferenceDatabase();
      me.run(url.toString(), username.toString(), pw.toString(), referenceDbFile.toString(), jdbcDriver.toString());
    }
  }
}
