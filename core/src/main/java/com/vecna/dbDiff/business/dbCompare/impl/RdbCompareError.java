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
 * Represents a difference between two relational DB schemas.
 *
 * @author dlopuch@vecna.com
 */
public class RdbCompareError {
  private final RdbCompareErrorType m_errorType;
  private final String m_message;

  /**
   * Create a new instance.
   *
   * @param errorType type of schema difference.
   * @param message descriptive message.
   */
  public RdbCompareError(RdbCompareErrorType errorType, String message) {
    m_errorType = errorType;
    m_message = message;
  }

  /**
   * @return type of the schema difference.
   */
  public RdbCompareErrorType getErrorType() {
    return m_errorType;
  }

  /**
   * @return descriptive message.
   */
  public String getMessage() {
    return m_message;
  }
}
