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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "idx"}))
public class Bar {
  private Long m_id;
  private String m_name;
  private int m_idx;
  private Map<String, Foo> m_foos = new HashMap<String, Foo>();

  @Id @GeneratedValue
  public Long getId() {
    return m_id;
  }

  public void setId(Long id) {
    m_id = id;
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public int getIdx() {
    return m_idx;
  }

  public void setIdx(int idx) {
    m_idx = idx;
  }

  @ManyToMany @MapKeyColumn(name = "mapkey")
  public Map<String, Foo> getFoos() {
    return m_foos;
  }

  public void setFoos(Map<String, Foo> foos) {
    m_foos = foos;
  }
}