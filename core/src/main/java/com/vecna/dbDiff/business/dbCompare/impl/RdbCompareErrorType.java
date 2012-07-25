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


/**
 * Enumeration for possible database comparison errors
 * @author dlopuch@vecna.com
 */
public enum RdbCompareErrorType {
  /** A testdb is missing a ref table */
  MISSING_TABLE,

  /** A testdb has an unexpected table */
  UNEXPECTED_TABLE,


  // Column Errors:
  //--------------
  /** A testdb table is missing a ref column */
  MISSING_COLUMN,

  /** A test column is of the wrong type */
  COL_TYPE_MISMATCH,

  /** Test column sql type code is wrong but the sql type name is correct **/
  COL_TYPE_WARNING,

  /** A test column has the wrong default */
  COL_DEFAULT_MISMATCH,

  /** A test column has the wrong nullability */
  COL_NULLABLE_MISMATCH,

  /** A test column has the wrong size */
  COL_SIZE_MISMATCH,

  /** A test column has the wrong ordinal */
  COL_ORDINAL_MISMATCH, //TODO: Make this a warning

  /** A test table has an extra column */
  UNEXPECTED_COLUMN,


  //Foreign Key Errors
  //--------------
  /** A test table is missing a FK */
  MISSING_FK,

  /** A test table's fk uses a column to reference another table just like a reference fk, but it's named differently from
   * the reference constraint */
  MISNAMED_FK,

  /** A test table contains a fk with the same name as a test fk, but it points to a different column */
  MISCONFIGURED_FK,

  /** A test FK looks the same as a reference fk, but it has the wrong sequence (implications for composite key's index structure)*/
  FK_SEQUENCE_MISMATCH,

  /** A test table has a fk constraint not in the reference table */
  UNEXPECTED_FK,

  /** Unexpected fk difference */
  UNKNOWN_FK_DIFF,

  // Index Errors:
  //--------------
  /** A testdb table is missing a ref index */
  MISSING_INDEX,

  /** A testdb table's index is missing a column */
  INDEX_MISSING_COLUMN,

  /** A testdb table has an unexpected index */
  UNEXPECTED_INDEX,

  /** A testdb table's index has an unexpected column */
  UNEXPECTED_INDEX_COL,

  /** An index is in both dbs with same name and column set but different column order */
  WRONG_INDEX_COL_ORDER,

  // Primary key:
  //--------------
  /** Test table is missing a primary key */
  MISSING_PRIMARY_KEY,
  /** Test table has a primary key but the reference table doesn't */
  UNEXPECTED_PRIMARY_KEY,
  /** Both table have primary keys but they span different columns */
  MISCONFIGURED_PRIMARY_KEY;


  private String message;

  /**
   * Set the message.
   * @param message The message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get the message.
   * @return Returns the message
   */
  public String getMessage() {
    return message;
  }

}
