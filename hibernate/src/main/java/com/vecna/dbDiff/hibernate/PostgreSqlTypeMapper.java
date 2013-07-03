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
