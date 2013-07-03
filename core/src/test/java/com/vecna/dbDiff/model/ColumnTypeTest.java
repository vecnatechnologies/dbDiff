/**
 * NOTICE: This software  source code and any of  its derivatives are the
 * confidential  and  proprietary   information  of  Vecna  Technologies,
 * Inc. (such source  and its derivatives are hereinafter  referred to as
 * "Confidential Information"). The  Confidential Information is intended
 * to be  used exclusively by  individuals or entities that  have entered
 * into either  a non-disclosure agreement or license  agreement (or both
 * of  these agreements,  if  applicable) with  Vecna Technologies,  Inc.
 * ("Vecna")   regarding  the  use   of  the   Confidential  Information.
 * Furthermore,  the  Confidential  Information  shall be  used  only  in
 * accordance  with   the  terms   of  such  license   or  non-disclosure
 * agreements.   All  parties using  the  Confidential Information  shall
 * verify that their  intended use of the Confidential  Information is in
 * compliance  with and  not in  violation of  any applicable  license or
 * non-disclosure  agreements.  Unless expressly  authorized by  Vecna in
 * writing, the Confidential Information  shall not be printed, retained,
 * copied, or  otherwise disseminated,  in part or  whole.  Additionally,
 * any party using the Confidential  Information shall be held liable for
 * any and  all damages incurred  by Vecna due  to any disclosure  of the
 * Confidential  Information (including  accidental disclosure).   In the
 * event that  the applicable  non-disclosure or license  agreements with
 * Vecna  have  expired, or  if  none  currently  exists, all  copies  of
 * Confidential Information in your  possession, whether in electronic or
 * printed  form, shall be  destroyed or  returned to  Vecna immediately.
 * Vecna  makes no  representations  or warranties  hereby regarding  the
 * suitability  of  the   Confidential  Information,  either  express  or
 * implied,  including  but not  limited  to  the  implied warranties  of
 * merchantability,    fitness    for    a   particular    purpose,    or
 * non-infringement. Vecna  shall not be liable for  any damages suffered
 * by  licensee as  a result  of  using, modifying  or distributing  this
 * Confidential Information.  Please email [info@vecnatech.com]  with any
 * questions regarding the use of the Confidential Information.
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
