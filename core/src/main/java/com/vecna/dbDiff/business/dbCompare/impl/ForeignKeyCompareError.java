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

import com.vecna.dbDiff.model.db.ForeignKey;

/**
 * Extends {@link RdbCompareError} to hold extended information specific to foreign key errors.
 *
 * @author ogolberg@vecna.com
 */
public class ForeignKeyCompareError extends RdbCompareError {
  private ForeignKey m_similarFk;

  /**
   * Create a new foreign key error.
   * @param errorType error type.
   * @param message message.
   * @param similarFk existing foreign key similar to the foreign key being tested.
   */
  public ForeignKeyCompareError(RdbCompareErrorType errorType, String message, ForeignKey similarFk) {
    super(errorType, message);
    m_similarFk = similarFk;
  }

  /**
   * @return an existing foreign key similar to the foreign key being tested
   */
  public ForeignKey getSimilarFk() {
    return m_similarFk;
  }
}
