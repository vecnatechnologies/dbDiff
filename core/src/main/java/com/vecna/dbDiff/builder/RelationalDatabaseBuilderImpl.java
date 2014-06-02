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

package com.vecna.dbDiff.builder;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.vecna.dbDiff.jdbc.MetadataFactory;
import com.vecna.dbDiff.model.CatalogSchema;
import com.vecna.dbDiff.model.ColumnType;
import com.vecna.dbDiff.model.TableType;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.db.ForeignKey;
import com.vecna.dbDiff.model.relationalDb.InconsistentSchemaException;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;
import com.vecna.dbDiff.model.relationalDb.RelationalIndex;
import com.vecna.dbDiff.model.relationalDb.RelationalTable;

/**
 * Builds a {@link RelationalDatabase} representation of a live database schema.
 *
 * @author dlopuch@vecna.com
 * @author ogolberg@vecna.com
 */
public class RelationalDatabaseBuilderImpl implements RelationalDatabaseBuilder {
  private final MetadataFactory m_metadataFactory;
  private ExecutorService m_executor = new ForkJoinPool();

  /**
   * Execute multiple tasks in parallel (scaling to the number of available cores). If an exception is thrown by one of the tasks, it is converted as specified below.
   * @param <T> task return type.
   * @param tasks tasks to execute.
   * @throws RelationalDatabaseReadException if one of the tasks throws a {@link SQLException} or a {@link RelationalDatabaseReadException}.
   * @throws InconsistentSchemaException if one of the tasks throws an {@link InconsistentSchemaException}.
   * @throws RuntimeException if one of the tasks throws any other exception.
   */
  private <T> void runInParallel(Collection<? extends Callable<T>> tasks) throws RelationalDatabaseReadException, InconsistentSchemaException, RuntimeException {
    Collection<Future<T>> futures;
    try {
      futures = m_executor.invokeAll(tasks);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }

    for (Future<T> future : futures) {
      try {
        future.get();
      } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        } else if (cause instanceof SQLException) {
          throw new RelationalDatabaseReadException(cause);
        } else {
          throw new RuntimeException(e);
        }
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  /**
   * Constructor that sets metadata based on a JDBC connection
   * @param metadataFactory a {@link MetadataFactory}.
   */
  public RelationalDatabaseBuilderImpl(MetadataFactory metadataFactory) {
    m_metadataFactory = metadataFactory;
  }

  /**
   * Retrieve all tables from a schema.
   * @param catalogSchema catalog/schema.
   * @return the tables.
   * @throws SQLException if thrown by the jdbc driver.
   */
  private List<RelationalTable> getTables(final CatalogSchema catalogSchema) throws SQLException {
    // Get the ResultSet of tables
    String[] tableTypes = {TableType.TABLE.name()};
    ResultSet rs = doGetTablesQuery(catalogSchema, tableTypes);

    // Build a set of Tables
    List<RelationalTable> tables = new ArrayList<RelationalTable>();

    while (rs.next()) {
      RelationalTable table = new RelationalTable(new CatalogSchema(rs.getString(1), rs.getString(2)), rs.getString(3));

      table.setType(rs.getString(4));
      table.setTypeName(rs.getString(5));

      tables.add(table);
    }

    return tables;
  }

  /**
   * Performs a metaData.getTables() query.
   * @param catalogSchema the desired catalog and schema names.
   * @param tableTypes the desired table types, specific for the particular implementation.
   * @return The ResultSet of the getTables() call.
   * @throws SQLException if thrown by the jdbc driver.
   */
  protected ResultSet doGetTablesQuery(CatalogSchema catalogSchema, String[] tableTypes) throws SQLException {
    return m_metadataFactory.getMetadata().getTables(catalogSchema.getCatalog(), catalogSchema.getSchema(), null, tableTypes);
  }

  /**
   * Retrieve column information for a table.
   * @param table the table.
   * @return ordered list of columns.
   * @throws SQLException if thrown by the jdbc driver.
   */
  private List<Column> getColumns(RelationalTable table) throws SQLException {
    ResultSet columnResultSet = m_metadataFactory.getMetadata().getColumns(table.getCatalogSchema().getCatalog(), table.getCatalogSchema().getSchema(), table.getName(), null);

    List<Column> columns = new LinkedList<Column>();
    while (columnResultSet.next()) {
      Column column = new Column(columnResultSet.getString(1), columnResultSet.getString(2),
                                 columnResultSet.getString(4), columnResultSet.getString(3));

      column.setColumnType(new ColumnType(columnResultSet.getInt(5), columnResultSet.getString(6)));
      column.setColumnSize(columnResultSet.getInt(7));

      //Nullability
      int nullable = columnResultSet.getInt(11);
      column.setIsNullable((DatabaseMetaData.columnNullable == nullable ? true
          : (DatabaseMetaData.columnNoNulls == nullable ? false : null)));

      column.setDefault(columnResultSet.getString(13));
      column.setOrdinal(columnResultSet.getInt(17));
      columns.add(column);
    }
    return columns;
  }

  /**
   * Retrieve foreign keys for a table.
   * @param table table.
   * @return ordered list of foreign keys.
   * @throws SQLException if thrown by the jdbc driver.
   */
  private List<ForeignKey> getForeignKeys(RelationalTable table) throws SQLException {
    ResultSet fkResultSet = m_metadataFactory.getMetadata().getImportedKeys(table.getCatalogSchema().getCatalog(), table.getCatalogSchema().getSchema(), table.getName());
    List<ForeignKey> fks = new LinkedList<ForeignKey>();
    while (fkResultSet.next()) {
      ForeignKey fk = new ForeignKey();
      fk.setFkName(fkResultSet.getString(12));

      fk.setFkCatalogSchema(new CatalogSchema(fkResultSet.getString(5), fkResultSet.getString(6)));
      fk.setFkTable(fkResultSet.getString(7));
      fk.setFkColumn(fkResultSet.getString(8));

      fk.setPkCatalogSchema(new CatalogSchema(fkResultSet.getString(1), fkResultSet.getString(2)));
      fk.setPkTable(fkResultSet.getString(3));
      fk.setPkColumn(fkResultSet.getString(4));

      fk.setKeySeq(fkResultSet.getString(9));
      fks.add(fk);
    }
    return fks;
  }

  /**
   * Retrieve index information for a table.
   * @param table the table.
   * @return list of indices.
   * @throws SQLException if thrown by the jdbc driver.
   */
  private List<RelationalIndex> getIndices(RelationalTable table) throws SQLException {
    List<RelationalIndex> indices = new ArrayList<>();

    // maps index name to column names
    Multimap<String, String> idxColumns = ArrayListMultimap.create();

    // one row per index-column pair
    ResultSet rs = m_metadataFactory.getMetadata().getIndexInfo(table.getCatalogSchema().getCatalog(),
                                                                table.getCatalogSchema().getSchema(),
                                                                table.getName(), false, false);

    while (rs.next()) {
      String idxName = rs.getString(6);
      Collection<String> columns = idxColumns.get(idxName);
      if (columns.isEmpty()) {
        // build a new index
        RelationalIndex index = new RelationalIndex(table.getCatalogSchema(), rs.getString(6));
        indices.add(index);
      }

      columns.add(rs.getString(9));
    }

    for (RelationalIndex index : indices) {
      List<Column> columns = new ArrayList<>(idxColumns.size());
      for (String columnName : idxColumns.get(index.getName())) {
        Column column = table.getColumnByName(columnName);

        if (column == null) {
          throw new InconsistentSchemaException("cannot find column " + columnName + " referenced by index " + index.getName() + " in table " + table.getName());
        }

        columns.add(column);
      }

      index.setColumns(columns);
    }

    return indices;
  }

  /**
   * Retrieve primary key information for a table.
   * @param table the table.
   * @return ordered list of primary key column names.
   * @throws SQLException if thrown by the jdbc driver.
   */
  private List<String> getPrimaryKeyColumns(RelationalTable table) throws SQLException {
    Map<Short, String> primaryKeys = new TreeMap<>();
    ResultSet rs = m_metadataFactory.getMetadata().getPrimaryKeys(table.getCatalogSchema().getCatalog(), table.getCatalogSchema().getSchema(), table.getName());
    while (rs.next()) {
      primaryKeys.put(rs.getShort(5), rs.getString(4));
    }
    return Lists.newArrayList(primaryKeys.values());
  }

  @Override
  public RelationalDatabase createRelationalDatabase(CatalogSchema catalogSchema) {
    //Grab all the tables
    List<RelationalTable> tables;
    try {
      tables = getTables(catalogSchema);
    } catch (SQLException e) {
      throw new RelationalDatabaseReadException("could not read table information", e);
    }

    // build columns, foreign and primary keys in parallel
    runInParallel(Collections2.transform(tables, new Function<RelationalTable, Callable<Void>>() {
      @Override
      public Callable<Void> apply(final RelationalTable table) {
        return new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            table.setColumns(getColumns(table));
            table.setFks(new HashSet<>(getForeignKeys(table)));
            table.setPkColumns(getPrimaryKeyColumns(table));
            table.setIndices(getIndices(table));
            return null;
          }
        };
      }
    }));

    return new RelationalDatabase(tables);
  }
}