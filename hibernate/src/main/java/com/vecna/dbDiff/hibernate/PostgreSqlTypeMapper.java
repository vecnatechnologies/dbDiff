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

import org.hibernate.type.BooleanType;
import org.hibernate.type.descriptor.sql.BitTypeDescriptor;
import org.hibernate.type.descriptor.sql.BooleanTypeDescriptor;

import com.google.common.collect.ImmutableMap;
import com.vecna.dbDiff.model.ColumnType;
import com.vecna.dbDiff.model.db.Column;

/**
 * This class defines and applies column data type mapping between Hibernate and PostgreSQL database.
 * @author greg.zheng@vecna.com
 */
public class PostgreSqlTypeMapper implements HibernateSqlTypeMapper {

  private static final ImmutableMap<ColumnType, ColumnType> TYPE_MAPPINGS =
      ImmutableMap.of(new ColumnType(BitTypeDescriptor.INSTANCE.getSqlType(), "bool"),
                      new ColumnType(BooleanTypeDescriptor.INSTANCE.getSqlType(), BooleanType.INSTANCE.getName()),
                      new ColumnType(BooleanTypeDescriptor.INSTANCE.getSqlType(), BooleanType.INSTANCE.getName()),
                      new ColumnType(BitTypeDescriptor.INSTANCE.getSqlType(), "bool"));

  /**
   *
   * {@inheritDoc}
   */
  @Override
  public void mapType(Column column) {
    ColumnType newColumnType = TYPE_MAPPINGS.get(new ColumnType(column.getType(), column.getTypeName()));
    if (newColumnType != null) {
      column.setColumnType(newColumnType);
    }
  }
}
