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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.UniqueKey;

import com.google.common.collect.ImmutableSet;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.ColumnType;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.db.ForeignKey;
import com.vecna.dbDiff.model.db.Table;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;
import com.vecna.dbDiff.model.relationalDb.RelationalIndex;
import com.vecna.dbDiff.model.relationalDb.RelationalTable;

/**
 * Creates DbDiff relational database model from Hibernate mappings
 * @author ogolberg@vecna.com
 */
public class HibernateMappingsConverter {
  private static final String DEFAULT_KEY_SEQ = "1";
  private static final List<DbSpecificMappingInfo> DB_MAPPING =  Arrays.asList(new DbSpecificMappingInfo("PostgreSQL",
                                                                                                         new DbNameTruncateInfo(63, 63),
                                                                                                         new PostgreSqlTypeMapper()));

  private static final ImmutableSet<Integer> NUMERIC_TYPES = ImmutableSet.of(
                                                                             Types.BIGINT,
                                                                             Types.BOOLEAN,
                                                                             Types.BIT,
                                                                             Types.DECIMAL,
                                                                             Types.TINYINT,
                                                                             Types.SMALLINT,
                                                                             Types.INTEGER,
                                                                             Types.FLOAT,
                                                                             Types.DOUBLE,
                                                                             Types.NUMERIC,
                                                                             Types.REAL
      );

  private final CatalogSchema m_catalogSchema;
  private final Configuration m_configuration;
  private final Mapping m_mapping;
  private final Dialect m_dialect;
  private final DbSpecificMappingInfo m_dbSpecificMappingInfo;

  /**
   * Extract the name of the table as it would appear in the database.
   * @param table hibernate table model.
   * @return the name of the table as it would appear in the database.
   */
  private String getTableName(org.hibernate.mapping.Table table) {
    return m_dbSpecificMappingInfo.getTruncateInfo().truncateTableName(table.getName().toLowerCase());
  }

  /**
   * Extract the name of the column as it would appear in the database.
   * @param column hibernate column model.
   * @return the name of the column as it would appear in the database.
   */
  private String getColumnName(org.hibernate.mapping.Column column) {
    return m_dbSpecificMappingInfo.getTruncateInfo().truncateColumnName(column.getName().toLowerCase());
  }

  /**
   * Create a new converter instance
   * @param catalogSchema default catalog/schema information
   * @param configuration hibernate configuration
   * @param mapping hibernate mapping
   */
  public HibernateMappingsConverter(CatalogSchema catalogSchema, Configuration configuration, Mapping mapping) {
    m_catalogSchema = catalogSchema;
    m_configuration = configuration;
    m_mapping = mapping;

    m_dialect = getDialect(m_configuration);
    m_dbSpecificMappingInfo = getDbSpecificMappingInfo(m_dialect);
  }

  /**
   * Create a new converted instance
   * @param catalogSchema default catalog/schema information
   * @param configuration hibernate configuration (the mapping will be built from the configuration)
   */
  public HibernateMappingsConverter(CatalogSchema catalogSchema, Configuration configuration) {
    this(catalogSchema, configuration, configuration.buildMapping());
  }

  /**
   * Extract the dialect from a hibernate configuration.
   * @param hibernateConfiguration hibernate configuration.
   * @return the dialect object.
   */
  private Dialect getDialect(Configuration hibernateConfiguration) {
    String dialectClassName = hibernateConfiguration.getProperty("hibernate.dialect");
    if (dialectClassName == null) {
      throw new IllegalStateException("dialect is not set");
    }
    Class<?> dialectClass;

    try {
      dialectClass = Thread.currentThread().getContextClassLoader().loadClass(dialectClassName);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("can't load dialect class", e);
    }

    try {
      return (Dialect) dialectClass.newInstance();
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("can't create dialect", e);
    } catch (InstantiationException e) {
      throw new IllegalStateException("can't create dialect", e);
    }
  }

  /**
   * Get an instance of {@link DbSpecificMappingInfo} for the target DB based on the dialect used.
   * @param dialect the dialect to infer a {@link DbSpecificMappingInfo} from
   * @return an instance of {@link DbSpecificMappingInfo} that suits the target DB
   */
  private DbSpecificMappingInfo getDbSpecificMappingInfo(Dialect dialect) {
    String simpleName = dialect.getClass().getSimpleName();

    for (DbSpecificMappingInfo info : DB_MAPPING) {
      if (simpleName.startsWith(info.getShortDialectName())) {
        return info;
      }
    }

    return new DbSpecificMappingInfo("", DbNameTruncateInfo.noTruncate(), new NoopSqlTypeMapper());
  }

  /**
   * Convert Hibernate mappings to DbDiff RelationalDatabase
   * @return a RelationalDatabase representation of the hibernate mappings
   */
  public RelationalDatabase convert() {
    List<RelationalTable> tables = new ArrayList<>();
    Iterator<org.hibernate.mapping.Table> mappedTables = m_configuration.getTableMappings();

    while (mappedTables.hasNext()) {
      tables.add(convertTable(mappedTables.next()));
    }

    RelationalDatabase rdb = new RelationalDatabase();
    rdb.setTables(tables);
    return rdb;
  }

  /**
   * Convert a Hibernate table model to the DbDiff table model.
   * @param mappedTable hibernate table.
   * @return DbDiff table.
   */
  private RelationalTable convertTable(org.hibernate.mapping.Table mappedTable) {
    RelationalTable table = new RelationalTable();
    Table tableTable = new Table();
    tableTable.setName(getTableName(mappedTable));

    tableTable.setSchema(m_catalogSchema.getSchema());
    tableTable.setCatalog(m_catalogSchema.getCatalog());

    table.setTable(tableTable);

    List<Column> columns = new ArrayList<>();
    List<RelationalIndex> indices = new ArrayList<>();

    @SuppressWarnings("unchecked")
    Iterator<org.hibernate.mapping.Column> mappedColumns = mappedTable.getColumnIterator();
    int idx = 1;
    while (mappedColumns.hasNext()) {
      org.hibernate.mapping.Column mappedColumn = mappedColumns.next();
      Column column = convertColumn(mappedColumn, mappedTable, idx++);
      columns.add(column);
      if (mappedColumn.isUnique()) {
        indices.add(getUniqueIndex(table, column));
      }
    }

    table.setColumns(columns);

    Set<ForeignKey> fkeys = new HashSet<>();
    @SuppressWarnings("unchecked")
    Iterator<org.hibernate.mapping.ForeignKey> mappedKeys = mappedTable.getForeignKeyIterator();
    while (mappedKeys.hasNext()) {
      ForeignKey fkey = convertForeignKey(mappedKeys.next());
      if (fkey != null) {
        fkeys.add(fkey);
      }
    }

    table.setFks(fkeys);

    @SuppressWarnings("unchecked")
    Iterator<Index> mappedIndices = mappedTable.getIndexIterator();

    while (mappedIndices.hasNext()) {
      indices.add(convertIndex(mappedIndices.next(), table));
    }

    @SuppressWarnings("unchecked")
    Iterator<UniqueKey> mappedUniqueKeys = mappedTable.getUniqueKeyIterator();
    while (mappedUniqueKeys.hasNext()) {
      indices.add(convertIndex(mappedUniqueKeys.next(), table));
    }

    if (mappedTable.getPrimaryKey() != null) {
      indices.add(convertIndex(mappedTable.getPrimaryKey(), table));
      List<String> pkColumnNames = new ArrayList<>();
      @SuppressWarnings("unchecked")
      Iterator<org.hibernate.mapping.Column> pkColumns = mappedTable.getPrimaryKey().getColumnIterator();
      while (pkColumns.hasNext()) {
        pkColumnNames.add(getColumnName(pkColumns.next()));
      }
      table.setPkColumns(pkColumnNames);
    }

    table.setIndices(indices);

    return table;
  }

  /**
   * Create a {@link RelationalIndex} representation of a column's unique constraint.
   * @param table the table that contains the column.
   * @param column the column with a unique constraint.
   * @return a {@link RelationalIndex} representation of the constraint.
   */
  private RelationalIndex getUniqueIndex(RelationalTable table, Column column) {
    RelationalIndex index = new RelationalIndex();
    Table tableDef = new Table();
    tableDef.setCatalog(table.getTable().getCatalog());
    tableDef.setSchema(table.getTable().getSchema());

    index.setTable(tableDef);

    index.setColumns(new ArrayList<Column>(Collections.singletonList(column)));
    return index;
  }

  /**
   * Convert a Hibernate constraint (unique key, primary key) representation to a {@link RelationalIndex}.
   * @param mappedConstraint hibernate constraint.
   * @param table the table the constraint applies to.
   * @return a {@link RelationalIndex} representation of the same constraint
   */
  private RelationalIndex convertIndex(Constraint mappedConstraint, RelationalTable table) {
    @SuppressWarnings("unchecked")
    Iterator<org.hibernate.mapping.Column> mappedColumns = mappedConstraint.getColumnIterator();
    return convertIndex(null, mappedColumns, table);
  }

  /**
   * Convert a Hibernate index representation to a {@link RelationalIndex}.
   * @param mappedIndex hibernate index.
   * @param table the table the index applies to.
   * @return a {@link RelationalIndex} representation of the index.
   */
  private RelationalIndex convertIndex(Index mappedIndex, RelationalTable table) {
    @SuppressWarnings("unchecked")
    Iterator<org.hibernate.mapping.Column> mappedColumns = mappedIndex.getColumnIterator();
    return convertIndex(StringUtils.lowerCase(mappedIndex.getName()), mappedColumns, table);
  }

  /**
   * Convert a Hibernate index model to a {@link RelationalIndex}.
   * @param name index name.
   * @param mappedColumns columns spanned by the index.
   * @param table the table that the index belongs to.
   * @return a {@link RelationalIndex} representation of the index.
   */
  private RelationalIndex convertIndex(String name, Iterator<org.hibernate.mapping.Column> mappedColumns, RelationalTable table) {
    List<Column> columns = new ArrayList<>();

    while (mappedColumns.hasNext()) {
      columns.add(table.getColumnByName(getColumnName(mappedColumns.next())));
    }
    RelationalIndex index = new RelationalIndex();
    Table indexTable = new Table();
    indexTable.setName(name);

    indexTable.setSchema(m_catalogSchema.getSchema());
    indexTable.setCatalog(m_catalogSchema.getCatalog());

    index.setTable(indexTable);
    index.setColumns(columns);

    return index;
  }

  /**
   * Convert a Hibernate foreign key object to a {@link ForeignKey}.
   * @param mappedKey hibernate foreign key.
   * @return a {@link ForeignKey} representation of the same foreign key.
   */
  private ForeignKey convertForeignKey(org.hibernate.mapping.ForeignKey mappedKey) {
    org.hibernate.mapping.Column column = mappedKey.getColumn(0);
    org.hibernate.mapping.Table table = mappedKey.getTable();

    org.hibernate.mapping.Table referencedTable = mappedKey.getReferencedTable();
    org.hibernate.mapping.Column referencedColumn;

    if (mappedKey.getReferencedColumns().size() == 0) {
      referencedColumn = referencedTable.getPrimaryKey().getColumn(0);
    } else {
      referencedColumn = (org.hibernate.mapping.Column) mappedKey.getReferencedColumns().get(0);
    }

    ForeignKey fkey = new ForeignKey();
    fkey.setFkCatalog(StringUtils.lowerCase(table.getCatalog()));
    fkey.setFkColumn(getColumnName(column));
    fkey.setFkName(mappedKey.getName().toLowerCase());

    fkey.setFkSchema(m_catalogSchema.getSchema());
    fkey.setFkCatalog(m_catalogSchema.getCatalog());

    fkey.setFkTable(getTableName(table));

    fkey.setKeySeq(DEFAULT_KEY_SEQ);

    fkey.setPkCatalog(StringUtils.lowerCase(referencedTable.getCatalog()));
    fkey.setPkColumn(getColumnName(referencedColumn));

    fkey.setPkSchema(m_catalogSchema.getSchema());
    fkey.setPkCatalog(m_catalogSchema.getCatalog());
    fkey.setPkTable(getTableName(referencedTable));

    return fkey;
  }

  /**
   * Convert a Hibernate column representation to a {@link Column} object.
   * @param mappedColumn the Hibernate column.
   * @param owner the table that contains the column.
   * @param ordinal column's ordinal in the table.
   * @return a {@link Column} representation of the same column.
   */
  private Column convertColumn(org.hibernate.mapping.Column mappedColumn, org.hibernate.mapping.Table owner, int ordinal) {
    Column column = new Column();
    ColumnType type = new ColumnType(mappedColumn.getSqlTypeCode(m_mapping), mappedColumn.getSqlType(m_dialect, m_mapping));
    column.setColumnType(type);

    m_dbSpecificMappingInfo.getTypeMapper().mapType(column);

    if (NUMERIC_TYPES.contains(column.getType())) {
      if (mappedColumn.getPrecision() != org.hibernate.mapping.Column.DEFAULT_PRECISION) {
        column.setColumnSize(mappedColumn.getPrecision());
      }
    } else if ("character".equals(mappedColumn.getValue().getType().getName())) {
      column.setColumnSize(1);
    } else if (!"binary".equals(mappedColumn.getValue().getType().getName())
        && mappedColumn.getLength() != org.hibernate.mapping.Column.DEFAULT_LENGTH) {
      column.setColumnSize(mappedColumn.getLength());
    }

    column.setDefault(mappedColumn.getDefaultValue());
    column.setName(getColumnName(mappedColumn));

    boolean notNull = !mappedColumn.isNullable()
        || (owner.getPrimaryKey() != null && owner.getPrimaryKey().getColumns().contains(mappedColumn));
    column.setIsNullable(!notNull);

    column.setCatalog(StringUtils.lowerCase(owner.getCatalog()));
    column.setSchema(StringUtils.lowerCase(owner.getSchema()));
    column.setOrdinal(ordinal);
    column.setTable(getTableName(owner));

    column.setDefault(mappedColumn.getDefaultValue());

    return column;
  }
}
