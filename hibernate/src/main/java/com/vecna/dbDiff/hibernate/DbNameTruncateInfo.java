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

/**
 * Contains information on how a particular database auto-truncates the names of columns and tables from DDL/SQL statements.
 *
 * @author ogolberg@vecna.com
 */
public class DbNameTruncateInfo {
  /**
   * @return an instance of {@link DbNameTruncateInfo} that performs no truncation.
   */
  public static DbNameTruncateInfo noTruncate() {
    return new DbNameTruncateInfo(null, null);
  }

  private final Integer m_maxColumnNameLength;
  private final Integer m_maxTableNameLength;

  private String truncate(String s, Integer max) {
    if (max == null || max >= s.length()) {
      return s;
    } else {
      return s.substring(0, max);
    }
  }

  /**
   * Truncate the name of a table.
   * @param tableName the name to truncate
   * @return truncated name
   */
  public String truncateTableName(String tableName) {
    return truncate(tableName, m_maxTableNameLength);
  }

  /**
   * Truncate the name of a column
   * @param columnName the name to truncate
   * @return truncated name
   */
  public String truncateColumnName(String columnName) {
    return truncate(columnName, m_maxColumnNameLength);
  }

  /**
   * Create a new instance.
   * @param maxColumnNameLength see {@link #getMaxColumnNameLength()}
   * @param maxTableNameLength see {@link #getMaxTableNameLength()}
   */
  public DbNameTruncateInfo(Integer maxColumnNameLength, Integer maxTableNameLength) {
    m_maxColumnNameLength = maxColumnNameLength;
    m_maxTableNameLength = maxTableNameLength;
  }

  /**
   * @return maximum length of a column name or <code>null</code> if there is no maximum or auto-truncation is not supported by the db
   */
  public Integer getMaxColumnNameLength() {
    return m_maxColumnNameLength;
  }

  /**
   * @return maximum length of a table name or <code>null</code> if there is no maximum or auto-truncation is not supported by the db
   */
  public Integer getMaxTableNameLength() {
    return m_maxTableNameLength;
  }
}
