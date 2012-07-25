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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Index;

@Entity
@org.hibernate.annotations.Table(appliesTo = "Foo", indexes = @Index(name = "foo_idx_name_time", columnNames = {"name", "time"}))
public class Foo {
  private Long m_id;
  private String m_name;
  private Date m_time;

  @Id @GeneratedValue
  public Long getId() {
    return m_id;
  }

  public void setId(Long id) {
    m_id = id;
  }

  @Column(nullable = false, length = 1000, unique = true)
  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public Date getTime() {
    return m_time;
  }

  public void setTime(Date time) {
    m_time = time;
  }
}
