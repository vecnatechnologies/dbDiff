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
 * Encapsulates DB-specific Hibernate-SQL mapping information not captured in the Hibernate dialect.
 * @author ogolberg@vecna.com
 */
public class DbSpecificMappingInfo {
  private final String m_shortDialectName;
  private final DbNameTruncateInfo m_truncateInfo;
  private final HibernateSqlTypeMapper m_typeMapper;

  /**
   * Create a new instance
   * @param shortDialectName truncated name of the Hibernate dialect that identifies the target DB
   * (e.g. "PostgreSQL" for PostgreSQL82Dialect, PostgreSQL81Dialect etc.).
   * @param truncateInfo name auto-truncate settings (see {@link DbNameTruncateInfo})
   * @param typeMapper type mapping information (see {@link HibernateSqlTypeMapper})
   */
  public DbSpecificMappingInfo(String shortDialectName, DbNameTruncateInfo truncateInfo, HibernateSqlTypeMapper typeMapper) {
    m_shortDialectName = shortDialectName;
    m_truncateInfo = truncateInfo;
    m_typeMapper = typeMapper;
  }

  /**
   * @return truncated name of the Hibernate dialect that identifies the target DB
   * (e.g. "PostgreSQL" for PostgreSQL82Dialect, PostgreSQL81Dialect etc.).
   */
  public String getShortDialectName() {
    return m_shortDialectName;
  }

  /**
   * @return name auto-truncate settings (see {@link DbNameTruncateInfo})
   */
  public DbNameTruncateInfo getTruncateInfo() {
    return m_truncateInfo;
  }

  /**
   * @return type mapping information (see {@link HibernateSqlTypeMapper})
   */
  public HibernateSqlTypeMapper getTypeMapper() {
    return m_typeMapper;
  }
}
