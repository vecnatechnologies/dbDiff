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

package com.vecna.dbDiff.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

/**
 * An implementation of {@link MetadataFactory} that creates a connection per thread.
 *
 * @author ogolberg@vecna.com
 */
public class ThreadLocalMetadataFactory implements MetadataFactory {
  private final Collection<Connection> m_connections = new Vector<>();

  private final ThreadLocal<DatabaseMetaData> m_threadLocalMetadata = new ThreadLocal<DatabaseMetaData>() {
    @Override
    protected DatabaseMetaData initialValue() {
      try {
        Connection connection = DriverManager.getConnection(m_url, m_username, m_password);
        m_connections.add(connection);
        return connection.getMetaData();
      } catch (SQLException e) {
        throw new RuntimeException("could not retrieve jdbc metadata", e);
      }
    };
  };

  private final String m_url;

  private final String m_username;
  private final String m_password;

  /**
   * Create a new factory.
   * @param url jdbc url.
   * @param username jdbc url.
   * @param password jdbc password.
   */
  public ThreadLocalMetadataFactory(String url, String username, String password) {
    m_url = url;
    m_username = username;
    m_password = password;
  }

  @Override
  public DatabaseMetaData getMetadata() {
    return m_threadLocalMetadata.get();
  }

  /**
   * Closes all jdbc connections opened by this factory.
   * @throws IOException if a connection cannot be closed.
   */
  @Override
  public void close() throws IOException {
    for (Connection connection : m_connections) {
      try {
        connection.close();
      } catch (SQLException e) {
        throw new IOException(e);
      }
    }
  }
}
