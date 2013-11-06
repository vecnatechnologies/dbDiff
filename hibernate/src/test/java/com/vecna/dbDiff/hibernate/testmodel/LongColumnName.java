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

package com.vecna.dbDiff.hibernate.testmodel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class LongColumnName {
  private Long m_id;
  private LongTableName m_stuff;
  private boolean m_active;

  @Id @GeneratedValue
  public Long getId() {
    return m_id;
  }

  public void setId(Long id) {
    m_id = id;
  }

  public boolean isActive() {
    return m_active;
  }

  public void setActive(boolean active) {
    m_active = active;
  }

  @ManyToOne(optional = false)
  @JoinColumn(name = "the_name_of_this_column_is_very_very_long_for_no_reason_whatsoever")
  public LongTableName getStuff() {
    return m_stuff;
  }

  public void setStuff(LongTableName stuff) {
    m_stuff = stuff;
  }
}
