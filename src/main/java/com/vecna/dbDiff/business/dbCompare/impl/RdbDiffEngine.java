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

import java.util.Collection;
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

  public List<RdbCompareError> compareRelationalDatabase(RelationalDatabase refDb, RelationalDatabase testDb) {
    List<RdbCompareError> errors = new LinkedList<RdbCompareError>();

    // First check every test table exists in the reference db
    for (RelationalTable testT : testDb.getTables()) {
      RelationalTable refT = refDb.getTableByName(testT.getTable().getName());
      if (refT == null) {
        RdbCompareError e = new RdbCompareError(RdbCompareErrorType.UNEXPECTED_TABLE,
                                                "Test table '" + testT.getTable().getName() + "' is not in expected db");
        errors.add(e);
      } else {
        // If the table exists in ref db, compare the two
        compareRelationalTables(refT, testT, errors);
      }
    }


    // Check every reference table exists in the test db
    for (RelationalTable refT : refDb.getTables()) {
      if (testDb.getTableByName(refT.getTable().getName()) == null) {
        RdbCompareError e = new RdbCompareError(RdbCompareErrorType.MISSING_TABLE,
                                                "Reference Table '" + refT.getTable().getName() + "' is missing");
        errors.add(e);
      }
    }

    return errors;
  }

  /**
   * Compares two relational tables
   * @param refT a reference RelationalTable
   * @param testT a test RelationalTable
   * @param errors a non-null list of errors to append new ones to
   */
  public void compareRelationalTables(RelationalTable refT, RelationalTable testT, List<RdbCompareError> errors) {
    // Compare primary key
    comparePrimaryKeys(refT, testT, errors);

    //Compare Columns
    compareColumns(refT, testT, errors);

    //Compare foreign keys
    compareForeignKeys(refT, testT, errors);

    // Compare indices
    compareIndices(refT, testT, errors);
  }

  private void comparePrimaryKeys(RelationalTable refT, RelationalTable testT, List<RdbCompareError> errors) {
    if (CollectionUtils.isEmpty(refT.getPkColumns())) {
      if (CollectionUtils.isNotEmpty(testT.getPkColumns())) {
        errors.add(new RdbCompareError(RdbCompareErrorType.UNEXPECTED_PRIMARY_KEY,
                                       "Test primary key " + testT.getTable().getName()
                                       + testT.getPkColumns() + " is unexpected!"));
      }
    } else if (CollectionUtils.isEmpty(testT.getPkColumns())) {
      if (CollectionUtils.isNotEmpty(refT.getPkColumns())) {
        errors.add(new RdbCompareError(RdbCompareErrorType.MISSING_PRIMARY_KEY,
                                       "Reference primary key " + refT.getTable().getName()
                                       + refT.getPkColumns() + " is missing!"));
      }
    } else if (!refT.getPkColumns().equals(testT.getPkColumns())) {
      errors.add(new RdbCompareError(RdbCompareErrorType.MISCONFIGURED_PRIMARY_KEY,
                                     "Test primary key " + testT.getTable().getName() + testT.getPkColumns()
                                     + " differs from reference primary key " + refT.getTable().getName() + refT.getPkColumns()));
    }
  }

  /**
   * Tests two tables' columns, checking for missing columns, column types, defaults, nullability, size, and ordinals.
   * Any errors get added to the errors param list.
   * @param refT A reference table
   * @param testT A test table
   * @param errors A list of errors to add any new errors to
   */
  private void compareColumns(RelationalTable refT, RelationalTable testT, List<RdbCompareError> errors) {
    //First check every test column exists in the reference table
    for (Column testC : testT.getColumns()) {
      Column refC = refT.getColumnByName(testC.getName());
      if (refC == null) {
        RdbCompareError e = new RdbCompareError(RdbCompareErrorType.UNEXPECTED_COLUMN,
                                  "Column '" + testT.getTable().getName() + "." + testC.getName() + "' is unexpected");
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
                                  "Test column '" + testT.getTable().getName() + "." + testC.getName() + "' has wrong type.  "
                                  + "Expected '" + refC.getType()+ "/" + refC.getTypeName()
                                  + "' but got '" + testC.getType() + "/" + testC.getTypeName() + "'");
          errors.add(e);
        }
        if (!Objects.equal(refC.getDefault(), testC.getDefault())) {
          RdbCompareError e = new RdbCompareError(RdbCompareErrorType.COL_DEFAULT_MISMATCH,
                                    "Test column '" + testT.getTable().getName() + "." + testC.getName() + "' has wrong Default.  "
                                    + "Expected '" + refC.getDefault()+ "' but got '" + testC.getDefault() + "'");
          errors.add(e);
        }
        if (!Objects.equal(refC.getIsNullable(), testC.getIsNullable())) {
          RdbCompareError e = new RdbCompareError(RdbCompareErrorType.COL_NULLABLE_MISMATCH,
                                    "Test column '" + testT.getTable().getName() + "." + testC.getName() + "' has wrong "
                                    + "nullability.  Expected '" + refC.getIsNullable() + "' but got '"
                                    + testC.getIsNullable() + "'");
          errors.add(e);
        }
        if (refC.getColumnSize() != null && testC.getColumnSize() != null && !refC.getColumnSize().equals(testC.getColumnSize())) {
          RdbCompareError e = new RdbCompareError(RdbCompareErrorType.COL_SIZE_MISMATCH,
                                    "Test column '" + testT.getTable().getName() + "." + testC.getName() + "' has wrong size.  "
                                    + "Expected '" + refC.getColumnSize() + "' but got '" + testC.getColumnSize() + "'");
          errors.add(e);
        }
        if (!Objects.equal(refC.getOrdinal(), refC.getOrdinal())) {
          //TODO: Turn this into a warning?
          RdbCompareError e = new RdbCompareError(RdbCompareErrorType.COL_ORDINAL_MISMATCH,
                                    "Test column '" + testT.getTable().getName() + "." + testC.getName() + "' has wrong ordinal.  "
                                    + "Expected '" + refC.getOrdinal() + "' but got '" + testC.getOrdinal() + "'");
          errors.add(e);
        }
      }
    }

    //Missing Columns: Check every ref col exists in test table
    for (Column refC : refT.getColumns()) {
      if (testT.getColumnByName(refC.getName()) == null) {
        RdbCompareError e = new RdbCompareError(RdbCompareErrorType.MISSING_COLUMN,
                                  "Table '" + testT.getTable().getName() + "' is missing column '" + refC.getName() + "'");
        errors.add(e);
      }
    }
  }

  /**
   * Tests two tables' foreign keys.
   * Any errors get added to the errors param list.
   * @param refT A reference table
   * @param testT A test table
   * @param errors A list of errors to add any new errors to
   */
  private void compareForeignKeys(RelationalTable refT, RelationalTable testT, List<RdbCompareError> errors) {
    List<ForeignKey> refFks = new LinkedList<ForeignKey>(refT.getFks());

    //Check that every test fk exists in the reference fk's
    testFks:
      for (ForeignKey testFk : testT.getFks()) {
        // Check by complete equality
        if (refFks.contains(testFk)) {
          //key is fine, remove it from the refFk's list and advance to the next fk
          refFks.remove(testFk);
          continue;

        //The test fk doesn't exist exactly in the reference set.  Figure out what's wrong
        } else {
          //Try to find a match based on constraint name
          Set<ForeignKey> refFksByName = refT.getFksByName(testFk.getFkName());
          if (CollectionUtils.isNotEmpty(refFksByName)) {
            for (ForeignKey refFk : refFksByName) {
              if (refFk.equalsFrom(testFk) && refFk.equalsReference(testFk)) {
                if (refFk.getKeySeq().equals(testFk.getKeySeq())) {
                  //FK with the same signature, name, and sequence number... something else is wrong
                  RdbCompareError e = new RdbCompareError(RdbCompareErrorType.UNKNOWN_FK_DIFF,
                                            "Test fk \"" + getFkDesc(testFk) + "\" has unknown difference with fk \""
                                            + getFkDesc(refFk) +"\".  Check the fk .equals() method and its hash-generation.");
                  errors.add(e);
                  refFks.remove(refFk);
                  continue testFks;
                } else {
                  //FK with the same signature and name, but wrong key sequence
                  RdbCompareError e = new RdbCompareError(RdbCompareErrorType.FK_SEQUENCE_MISMATCH,
                                            "Test fk '" + testFk.getFkName() + "' in table '" + testT.getTable().getName() + "' has"
                                            + " wrong key sequence. Expected '" + refFk.getKeySeq() + "' but got '"
                                            + testFk.getKeySeq() + "'");
                  errors.add(e);
                  refFks.remove(refFk);
                  continue testFks;
                }
              }
            }
            // No reference key by this name has the same to and from.  Misconfigured key.
            String matchingFkNames = new String();
            boolean delim = false;
            for (ForeignKey fk : refFksByName) {
              matchingFkNames += (delim ? ", \"" : "\"") + getFkDesc(fk) + "\"";
              delim = true;
            }
            RdbCompareError e = new RdbCompareError(RdbCompareErrorType.MISCONFIGURED_FK,
                                      "Test fk \"" + getFkDesc(testFk) + "\" has the same name as the following reference FK "
                                      + "constraint(s) but different signature: " + matchingFkNames);
            errors.add(e);
            continue testFks;
          } else {

            //Try to find a match based on reference
            Set<ForeignKey> refFksByRefCol = refT.getFksByReferencedCol(testFk.getPkCatalog(), testFk.getPkSchema(),
                                                                        testFk.getPkTable(), testFk.getPkColumn());
            if (CollectionUtils.isNotEmpty(refFksByRefCol)) {
              for (ForeignKey refFk : refFksByRefCol) {
                if (refFk.equalsFrom(testFk)) {
                  // We have a fk with same signature
                  if (refFk.getFkName().equals(testFk.getFkName())) {
                    //Same signature and name, unknown difference
                    RdbCompareError e = new RdbCompareError(RdbCompareErrorType.UNKNOWN_FK_DIFF,
                                              "Test fk \"" + getFkDesc(testFk) + "\" has unknown difference with fk \""
                                              + getFkDesc(refFk) +"\".  Check the fk .equals() method and its hash-generation.");
                    errors.add(e);
                    refFks.remove(refFk);
                    continue testFks;
                  } else {
                    //Same signature but different name: misnamed FK
                    RdbCompareError e = new RdbCompareError(RdbCompareErrorType.MISNAMED_FK,
                                              "Test fk \"" + getFkDesc(testFk) + "\" looks the same as the following fk but wrong"
                                              + " name: \"" + getFkDesc(refFk) +"\".");
                    errors.add(e);
                    refFks.remove(refFk);
                    continue testFks;
                  }
                }
              }
            } else {
              //Unexpected FK
              RdbCompareError e = new RdbCompareError(RdbCompareErrorType.UNEXPECTED_FK,
                                          "Test foreign key \"" + getFkDesc(testFk) + "\" is unexpected!");
              errors.add(e);
            }
          }

        }
      }
    //End testing the fk's

    //Missing FK's: Any test fk that had some partial match against a reference fk would have had the reference fk removed.
    //Any remaining reference fk's are missing ones.
    for (ForeignKey fk : refFks) {
      RdbCompareError e = new RdbCompareError(RdbCompareErrorType.MISSING_FK,
                                              "Reference foreign key \"" + getFkDesc(fk) + "\" is missing!");
      errors.add(e);
    }
  }

  private void compareIndices(final RelationalTable refT, final RelationalTable testT, List<RdbCompareError> errors) {
    Multimap<List<String>, RelationalIndex> refIndices = ArrayListMultimap.create(refT.getIndicesByColumns());

    for (final Entry<List<String>, Collection<RelationalIndex>> entry : testT.getIndicesByColumns().asMap().entrySet()) {
      Collection<RelationalIndex> matchingRefIndices = refIndices.removeAll(entry.getKey());
      if (CollectionUtils.isEmpty(matchingRefIndices)) {
        for (RelationalIndex testIndex : entry.getValue()) {
          errors.add(new RdbCompareError(RdbCompareErrorType.UNEXPECTED_INDEX,
                                         "Test index \"" + getIndexDesc(testIndex, testT) + "\" is unexpected!"));
        }
      } else {
        int testIndicesWithUnknownNames = 0;
        int refIndicesWithUnknownNames = 0;
        Set<String> testIndexNames = Sets.newHashSet();
        Set<String> refIndexNames = Sets.newHashSet();

        for (RelationalIndex refIndex : matchingRefIndices) {
          if (refIndex.getTable().getName() == null) {
            refIndicesWithUnknownNames++;
          } else {
            refIndexNames.add(refIndex.getTable().getName());
          }
        }

        for (RelationalIndex testIndex : entry.getValue()) {
          if (testIndex.getTable().getName() == null) {
            testIndicesWithUnknownNames++;
          } else {
            if (!refIndexNames.remove(testIndex.getTable().getName())) {
              testIndexNames.add(testIndex.getTable().getName());
            }
          }
        }

        if (refIndicesWithUnknownNames == 0 && !testIndexNames.isEmpty()) {
          for (String testIndexName : testIndexNames) {
            errors.add(new RdbCompareError(RdbCompareErrorType.UNEXPECTED_INDEX, "Test index \""
                                           + getIndexDesc(testIndexName, entry.getKey(), testT) + "\" is unexpected!"));
          }
        } else if (testIndexNames.size() > refIndicesWithUnknownNames) {
          errors.add(new RdbCompareError(RdbCompareErrorType.UNEXPECTED_INDEX,
                                         "At least " + (testIndexNames.size() - refIndicesWithUnknownNames)
                                         + " of test indices "
                                         + Joiner.on(", ").join(Collections2.transform(testIndexNames,
                                                                                      new Function<String, String>() {
            public String apply(String from) {
              return "\"" + getIndexDesc(from, entry.getKey(), testT) + "\"";
            }
          })) + " are unexpected!"));
        }


        if (testIndicesWithUnknownNames == 0 && !refIndexNames.isEmpty()) {
          for (String refIndexName : refIndexNames) {
            errors.add(new RdbCompareError(RdbCompareErrorType.MISSING_INDEX, "Reference index \""
                                           + getIndexDesc(refIndexName, entry.getKey(), refT) + "\" is missing!"));
          }
        } else if (refIndexNames.size() > testIndicesWithUnknownNames) {
          errors.add(new RdbCompareError(RdbCompareErrorType.MISSING_INDEX,
                                         "At least " + (refIndexNames.size() - testIndicesWithUnknownNames)
                                         + " of reference indices "
                                         + Joiner.on(", ").join(Collections2.transform(refIndexNames,
                                                                                      new Function<String, String>() {
            public String apply(String from) {
              return "\"" + getIndexDesc(from, entry.getKey(), refT) + "\"";
            }
          })) + " are missing!"));
        }


      }
    }
  }

  private String getIndexDesc(String indexName, Collection<String> columnNames, RelationalTable owner) {
    return (indexName == null ? "<UNKNOWN>" : indexName) + "="
    + owner.getTable().getName() + "(" + Joiner.on(',').join(columnNames) + ")";
  }

  private String getIndexDesc(RelationalIndex idx, RelationalTable owner) {
    return getIndexDesc(idx.getTable().getName(), Collections2.transform(idx.getColumns(), new Function<Column, String>() {
      public String apply(Column from) {
        return from.getName();
      }
    }), owner);
  }

  private String getFkDesc(ForeignKey fk) {
    return fk.getFkName() + "(" + fk.getKeySeq() + "): " + fk.getFkTable() + "(" + fk.getFkColumn() + ")-->" + fk.getPkTable()
           + "(" + fk.getPkColumn() + ")";
  }
}
