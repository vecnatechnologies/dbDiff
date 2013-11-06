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

package com.vecna.dbDiff.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.vecna.dbDiff.model.ColumnType;

/**
 * @author greg.zheng@vecna.com
 */
public class ColumnTypeTest {

  /**
   * Test null type name
   */
  @Test
  public void testNullTypeName() {
    ColumnType typeBool = new  ColumnType(-7, null);
    ColumnType typeBool2 = new  ColumnType(-7, null);
    assertEquals("Expected bool types are equal", typeBool, typeBool2);
  }

  /**
   * Test two ColumnType objects are equal
   */
  @Test
  public void testEquals() {
    ColumnType typeBool = new  ColumnType(-7, "bool");
    ColumnType typeBoolean = new  ColumnType(16, "boolean");
    ColumnType typeBool2 = new  ColumnType(-7, "bool");
    assertEquals("Expected bool types are equal", typeBool, typeBool2);
    assertNotSame("Expected bool and boolean are not equal", typeBool, typeBoolean);
  }

  /**
   * Test ColumnType object can be used as key
   */
  @Test
  public void testEquality() {
    Map<ColumnType, ColumnType> typeMap = new HashMap<ColumnType, ColumnType>();
    ColumnType typeBool = new  ColumnType(-7, "bool");
    ColumnType typeBoolean = new  ColumnType(16, "boolean");
    ColumnType typeBool2 = new  ColumnType(-7, "bool");
    typeMap.put(typeBool, typeBool2);
    typeMap.put(typeBoolean, typeBool2);
    assertEquals("Expected bool types are equal", typeBool, typeMap.get(typeBool));
    assertEquals("Expected bool types are equal", typeBool, typeMap.get(typeBoolean));
  }
}
