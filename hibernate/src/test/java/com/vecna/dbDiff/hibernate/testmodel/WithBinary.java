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
import javax.persistence.Id;

@Entity
public class WithBinary {
  private Long m_id;
  private byte[] m_data;

  @Id
  public Long getId() {
    return m_id;
  }

  public void setId(Long id) {
    m_id = id;
  }

  public byte[] getData() {
    return m_data;
  }

  public void setData(byte[] data) {
    m_data = data;
  }
}
