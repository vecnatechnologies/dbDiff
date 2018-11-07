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

package com.vecna.dbDiff.business.dbCompare.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.vecna.dbDiff.model.db.Column;
import com.vecna.dbDiff.model.db.ForeignKey;
import com.vecna.dbDiff.model.relationalDb.RelationalDatabase;
import com.vecna.dbDiff.model.relationalDb.RelationalIndex;
import com.vecna.dbDiff.model.relationalDb.RelationalTable;

/**
 * Business logic to compare two RelationalDatabase objects
 * @author dlopuch@vecna.com
 */
public class RdbDiffEngine {
  /**
   * Compare two relational DB schemas.
   * @param refDb reference database.
   * @param testDb test database.
   * @return the list of DB schema differences.
   */
  public List<RdbCompareError> compareRelationalDatabase(RelationalDatabase refDb, RelationalDatabase testDb) {
    List<RdbCompareError> errors = new LinkedList<RdbCompareError>();

    // First check every test table exists in the reference db
    for (RelationalTable testT : testDb.getTables()) {
      RelationalTable refT = refDb.getTableByName(testT.getName());
      if (refT == null) {
        RdbCompareError e = new RdbCompareError(RdbCompareErrorType.UNEXPECTED_TABLE,
                                                "Test table '" + testT.getName() + "' is not in expected db",
                                                RdbFoundOnSide.TEST);
        errors.add(e);
      } else {
        // If the table exists in ref db, compare the two
        errors.addAll(compareRelationalTables(refT, testT));
      }
    }

    // Check every reference table exists in the test db
    for (RelationalTable refT : refDb.getTables()) {
      if (testDb.getTableByName(refT.getName()) == null) {
        RdbCompareError e = new RdbCompareError(RdbCompareErrorType.MISSING_TABLE,
                                                "Reference Table '" + refT.getName() + "' is missing",
                                                 RdbFoundOnSide.REF);
        errors.add(e);
      }
    }

    return errors;
  }

  /**
   * Compares two relational tables
   * @param refT a reference RelationalTable
   * @param testT a test RelationalTable
   * @return list of table differences.
   */
  public List<RdbCompareError> compareRelationalTables(RelationalTable refT, RelationalTable testT) {
    List<RdbCompareError> errors = new ArrayList<>();

    // Compare primary key
    errors.addAll(comparePrimaryKeys(refT, testT));

    //Compare Columns
    errors.addAll(compareColumns(refT, testT));

    //Compare foreign keys
    errors.addAll(compareForeignKeys(refT, testT));

    // Compare indices
    errors.addAll(compareIndices(refT, testT));

    return errors;
  }

  /**
   * Compare primary keys.
   * @param refT reference table.
   * @param testT test table.
   * @return primary key differences.
   */
  private List<RdbCompareError> comparePrimaryKeys(RelationalTable refT, RelationalTable testT) {
    List<RdbCompareError> errors = new ArrayList<>();
    if (CollectionUtils.isEmpty(refT.getPkColumns())) {
      if (CollectionUtils.isNotEmpty(testT.getPkColumns())) {
        errors.add(new RdbCompareError(RdbCompareErrorType.UNEXPECTED_PRIMARY_KEY,
                                       "Test primary key " + testT.getName()
                                       + testT.getPkColumns() + " is unexpected!",
                                       RdbFoundOnSide.TEST));
      }
    } else if (CollectionUtils.isEmpty(testT.getPkColumns())) {
      if (CollectionUtils.isNotEmpty(refT.getPkColumns())) {
        errors.add(new RdbCompareError(RdbCompareErrorType.MISSING_PRIMARY_KEY,
                                       "Reference primary key " + refT.getName()
                                       + refT.getPkColumns() + " is missing!",
                                       RdbFoundOnSide.REF));
      }
    } else if (!refT.getPkColumns().equals(testT.getPkColumns())) {
      errors.add(new RdbCompareError(RdbCompareErrorType.MISCONFIGURED_PRIMARY_KEY,
                                     "Test primary key " + testT.getName() + testT.getPkColumns()
                                     + " differs from reference primary key " + refT.getName() + refT.getPkColumns(),
                                     RdbFoundOnSide.UNSPECIFIED));
    }
    return errors;
  }

  /**
   * Tests two tables' columns, checking for missing columns, column types, defaults, nullability, size, and ordinals.
   * Any errors get added to the errors param list.
   * @param refT A reference table
   * @param testT A test table
   * @return the list of column differences.
   */
  private List<RdbCompareError> compareColumns(RelationalTable refT, RelationalTable testT) {
    List<RdbCompareError> errors = new ArrayList<>();
    //First check every test column exists in the reference table
    for (Column testC : testT.getColumns()) {
      Column refC = refT.getColumnByName(testC.getName());
      if (refC == null) {
        RdbCompareError e = new RdbCompareError(RdbCompareErrorType.UNEXPECTED_COLUMN,
                                  "Column '" + testT.getName() + "." + testC.getName() + "' is unexpected",
                                   RdbFoundOnSide.TEST);
        errors.add(e);
      } else {
        //Column is expected.  Check the column properties
        if (!Objects.equal(refC.getType(), testC.getType())) {
          RdbCompareErrorType errorType;
          // if the codes are different but the type names match, issue a warning
          if (StringUtils.isNotEmpty(refC.getTypeName()) && refC.getTypeName().equals(testC.getTypeName())) {
            errorType = RdbCompareErrorType.COL_TYPE_WARNING;
          } else {
            errorType = RdbCompareErrorType.COL_TYPE_MISMATCH;
          }
          RdbCompareError e = new RdbCompareError(errorType,
                                  "Test column '" + testT.getName() + "." + testC.getName() + "' has wrong type.  "
                                  + "Expected '" + refC.getType() + "/" + refC.getTypeName()
                                  + "' but got '" + testC.getType() + "/" + testC.getTypeName() + "'",
                                  RdbFoundOnSide.UNSPECIFIED);
          errors.add(e);
        }
        if (!Objects.equal(refC.getDefault(), testC.getDefault())) {
          RdbCompareError e = new RdbCompareError(RdbCompareErrorType.COL_DEFAULT_MISMATCH,
                                    "Test column '" + testT.getName() + "." + testC.getName() + "' has wrong Default.  "
                                    + "Expected '" + refC.getDefault() + "' but got '" + testC.getDefault() + "'",
                                    RdbFoundOnSide.UNSPECIFIED);
          errors.add(e);
        }
        if (!Objects.equal(refC.getIsNullable(), testC.getIsNullable())) {
          RdbCompareError e = new RdbCompareError(RdbCompareErrorType.COL_NULLABLE_MISMATCH,
                                    "Test column '" + testT.getName() + "." + testC.getName() + "' has wrong "
                                    + "nullability.  Expected '" + refC.getIsNullable() + "' but got '"
                                    + testC.getIsNullable() + "'",
                                    RdbFoundOnSide.UNSPECIFIED);
          errors.add(e);
        }
        if (refC.getColumnSize() != null && testC.getColumnSize() != null && !refC.getColumnSize().equals(testC.getColumnSize())) {
          RdbCompareError e = new RdbCompareError(RdbCompareErrorType.COL_SIZE_MISMATCH,
                                    "Test column '" + testT.getName() + "." + testC.getName() + "' has wrong size.  "
                                    + "Expected '" + refC.getColumnSize() + "' but got '" + testC.getColumnSize() + "'",
                                    RdbFoundOnSide.UNSPECIFIED);
          errors.add(e);
        }
        if (!Objects.equal(refC.getOrdinal(), refC.getOrdinal())) {
          //TODO: Turn this into a warning?
          RdbCompareError e = new RdbCompareError(RdbCompareErrorType.COL_ORDINAL_MISMATCH,
                                    "Test column '" + testT.getName() + "." + testC.getName() + "' has wrong ordinal.  "
                                    + "Expected '" + refC.getOrdinal() + "' but got '" + testC.getOrdinal() + "'",
                                    RdbFoundOnSide.UNSPECIFIED);
          errors.add(e);
        }
      }
    }

    //Missing Columns: Check every ref col exists in test table
    for (Column refC : refT.getColumns()) {
      if (testT.getColumnByName(refC.getName()) == null) {
        RdbCompareError e = new RdbCompareError(RdbCompareErrorType.MISSING_COLUMN,
                                  "Table '" + testT.getName() + "' is missing column '" + refC.getName() + "'",
                                   RdbFoundOnSide.REF);
        errors.add(e);
      }
    }

    return errors;
  }

  /**
   * Determine why a test foreign key is not in the reference database.
   * @param testFk test foreign key which is not in the reference db.
   * @param testT test table the foreign key belongs to.
   * @param refT reference table that matches the test table.
   * @return a {@link ForeignKeyCompareError} specific to the foreign key.
   */
  private ForeignKeyCompareError getUnexpectedFkError(ForeignKey testFk, RelationalTable testT, RelationalTable refT) {
    Set<ForeignKey> refFksByName = refT.getFksByName(testFk.getFkName());

    if (!refFksByName.isEmpty()) {
      for (ForeignKey refFk : refFksByName) {
        if (refFk.equalsFrom(testFk) && refFk.equalsReference(testFk)) {
          if (refFk.getKeySeq().equals(testFk.getKeySeq())) {
            //FK with the same signature, name, and sequence number... something else is wrong
            return new ForeignKeyCompareError(RdbCompareErrorType.UNKNOWN_FK_DIFF,
                                              "Test fk \"" + testFk + "\" has unknown difference with fk \""
                                                  + refFk + "\".  Check the fk .equals() method and its hash-generation.", refFk);
          } else {
            //FK with the same signature and name, but wrong key sequence
            return new ForeignKeyCompareError(RdbCompareErrorType.FK_SEQUENCE_MISMATCH,
                                              "Test fk '" + testFk.getFkName() + "' in table '" + testT.getName() + "' has"
                                                  + " wrong key sequence. Expected '" + refFk.getKeySeq() + "' but got '"
                                                  + testFk.getKeySeq() + "'", refFk);
          }
        }
      }
      // No reference key by this name has the same to and from.  Misconfigured key.
      String matchingFkNames = Joiner.on(", ").join(refFksByName);

      return new ForeignKeyCompareError(RdbCompareErrorType.MISCONFIGURED_FK,
                                        "Test fk \"" + testFk + "\" has the same name as the following reference FK "
                                            + "constraint(s) but different signature: " + matchingFkNames, null);
    } else {
      //Try to find a match based on reference
      Set<ForeignKey> refFksByRefCol = refT.getFksByReferencedCol(testFk.getPkCatalogSchema().getCatalog(), testFk.getPkCatalogSchema().getSchema(),
                                                                  testFk.getPkTable(), testFk.getPkColumn());
      if (!refFksByRefCol.isEmpty()) {
        for (ForeignKey refFk : refFksByRefCol) {
          if (refFk.equalsFrom(testFk)) {
            // We have a fk with same signature
            if (refFk.getFkName().equals(testFk.getFkName())) {
              //Same signature and name, unknown difference
              return new ForeignKeyCompareError(RdbCompareErrorType.UNKNOWN_FK_DIFF,
                                                "Test fk \"" + testFk + "\" has unknown difference with fk \""
                                                    + refFk + "\".  Check the fk .equals() method and its hash-generation.", refFk);
            } else {
              //Same signature but different name: misnamed FK
              return new ForeignKeyCompareError(RdbCompareErrorType.MISNAMED_FK,
                                                "Test fk \"" + testFk + "\" looks the same as the following fk but wrong"
                                                    + " name: \"" + refFk + "\".", refFk);
            }
          }
        }

        String matchingFks = Joiner.on(", ").join(refFksByRefCol);

        return new ForeignKeyCompareError(RdbCompareErrorType.MISCONFIGURED_FK,
                                          "Test fk \"" + testFk + "\" references the same columns as the following reference FK "
                                              + "constraint(s) but applies to a different column: " + matchingFks, null);

      } else {
        //Unexpected FK
        return new ForeignKeyCompareError(RdbCompareErrorType.UNEXPECTED_FK,
                                          "Test foreign key \"" + testFk + "\" is unexpected!", null);
      }
    }
  }

  /**
   * Tests two tables' foreign keys.
   * Any errors get added to the errors param list.
   * @param refT A reference table
   * @param testT A test table
   * @return foreign key differences.
   */
  private List<RdbCompareError> compareForeignKeys(RelationalTable refT, RelationalTable testT) {
    List<RdbCompareError> errors = new ArrayList<>();
    Set<ForeignKey> refFks = new HashSet<>(refT.getFks());

      for (ForeignKey testFk : testT.getFks()) {
        if (!refFks.remove(testFk)) {
          ForeignKeyCompareError error = getUnexpectedFkError(testFk, testT, refT);
          if (error.getSimilarFk() != null) {
            refFks.remove(error.getSimilarFk());
          }
          errors.add(error);
        }
      }

    //Missing FK's: Any test fk that had some partial match against a reference fk would have had the reference fk removed.
    //Any remaining reference fk's are missing ones.
    for (ForeignKey fk : refFks) {
      errors.add(new RdbCompareError(RdbCompareErrorType.MISSING_FK, "Reference foreign key \"" + fk + "\" is missing!",
                         RdbFoundOnSide.REF));
    }
    return errors;
  }

  /**
   * Compare the indices of two tables.
   * @param refT reference table.
   * @param testT test table.
   * @return the list of index differences.
   */
  private List<RdbCompareError> compareIndices(final RelationalTable refT, final RelationalTable testT) {
    Multimap<List<String>, RelationalIndex> refIndices = ArrayListMultimap.create(refT.getIndicesByColumns());
    List<RdbCompareError> errors = new ArrayList<>();

    for (final Entry<List<String>, Collection<RelationalIndex>> entry : testT.getIndicesByColumns().asMap().entrySet()) {
      Collection<RelationalIndex> matchingRefIndices = refIndices.removeAll(entry.getKey());
      if (CollectionUtils.isEmpty(matchingRefIndices)) {
        for (RelationalIndex testIndex : entry.getValue()) {
          errors.add(new RdbCompareError(RdbCompareErrorType.UNEXPECTED_INDEX,
                                         "Test index \"" + getIndexDesc(testIndex, testT) + "\" is unexpected!",
                                          RdbFoundOnSide.TEST));
        }
      } else {
        int testIndicesWithUnknownNames = 0;
        int refIndicesWithUnknownNames = 0;
        Set<String> testIndexNames = Sets.newHashSet();
        Set<String> refIndexNames = Sets.newHashSet();

        for (RelationalIndex refIndex : matchingRefIndices) {
          if (refIndex.getName() == null) {
            refIndicesWithUnknownNames++;
          } else {
            refIndexNames.add(refIndex.getName());
          }
        }

        for (RelationalIndex testIndex : entry.getValue()) {
          if (testIndex.getName() == null) {
            testIndicesWithUnknownNames++;
          } else {
            if (!refIndexNames.remove(testIndex.getName())) {
              testIndexNames.add(testIndex.getName());
            }
          }
        }

        if (refIndicesWithUnknownNames == 0 && !testIndexNames.isEmpty()) {
          for (String testIndexName : testIndexNames) {
            errors.add(new RdbCompareError(RdbCompareErrorType.UNEXPECTED_INDEX, "Test index \""
                                           + getIndexDesc(testIndexName, entry.getKey(), testT) + "\" is unexpected!",
                                            RdbFoundOnSide.TEST));
          }
        } else if (testIndexNames.size() > refIndicesWithUnknownNames) {
          errors.add(new RdbCompareError(RdbCompareErrorType.UNEXPECTED_INDEX,
                                         "At least " + (testIndexNames.size() - refIndicesWithUnknownNames)
                                         + " of test indices "
                                         + Joiner.on(", ").join(Collections2.transform(testIndexNames,
                                                                                      new Function<String, String>() {
            @Override
            public String apply(String from) {
              return "\"" + getIndexDesc(from, entry.getKey(), testT) + "\"";
            }
          })) + " are unexpected!", RdbFoundOnSide.TEST));
        }


        if (testIndicesWithUnknownNames == 0 && !refIndexNames.isEmpty()) {
          for (String refIndexName : refIndexNames) {
            errors.add(new RdbCompareError(RdbCompareErrorType.MISSING_INDEX, "Reference index \""
                                           + getIndexDesc(refIndexName, entry.getKey(), refT) + "\" is missing!",
                    RdbFoundOnSide.REF));
          }
        } else if (refIndexNames.size() > testIndicesWithUnknownNames) {
          errors.add(new RdbCompareError(RdbCompareErrorType.MISSING_INDEX,
                                         "At least " + (refIndexNames.size() - testIndicesWithUnknownNames)
                                         + " of reference indices "
                                         + Joiner.on(", ").join(Collections2.transform(refIndexNames,
                                                                                      new Function<String, String>() {
            @Override
            public String apply(String from) {
              return "\"" + getIndexDesc(from, entry.getKey(), refT) + "\"";
            }
          })) + " are missing!", RdbFoundOnSide.REF));
        }
      }
    }
    return errors;
  }

  /**
   * Create a human-readable description of a table index.
   * @param indexName index name.
   * @param columnNames names of the columns.
   * @param owner the table that the index belongs to.
   * @return a human-readable description of the index.
   */
  private String getIndexDesc(String indexName, Collection<String> columnNames, RelationalTable owner) {
    return (indexName == null ? "<UNKNOWN>" : indexName) + "="
    + owner.getName() + "(" + Joiner.on(',').join(columnNames) + ")";
  }

  /**
   * Create a human-readable description of a table index.
   * @param idx index model.
   * @param owner the table that owns the index.
   * @return a human-readable description of the index.
   */
  private String getIndexDesc(RelationalIndex idx, RelationalTable owner) {
    return getIndexDesc(idx.getName(), Collections2.transform(idx.getColumns(), new Function<Column, String>() {
      @Override
      public String apply(Column from) {
        return from.getName();
      }
    }), owner);
  }
}
