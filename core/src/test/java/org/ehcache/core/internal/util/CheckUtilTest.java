/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.core.internal.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Henri Tremblay
 */
public class CheckUtilTest {

  @Test
  public void checkNonNull_null() {
    assertThatThrownBy(() -> CheckUtil.checkNonNull((Object) null))
      .isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  public void checkNonNull_notNull() {
    CheckUtil.checkNonNull("a");
  }

  @Test
  public void checkNonNull1_null() {
    assertThatThrownBy(() -> CheckUtil.checkNonNull("a", null, "b"))
      .isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  public void checkNonNull1_notNull() {
    CheckUtil.checkNonNull("a", "c", "b");
  }

  @Test
  public void checkNonNullContent_nullCollection() {
    assertThatThrownBy(() -> CheckUtil.checkNonNullContent(null))
      .isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  public void checkNonNullContent_nullElement() {
    assertThatThrownBy(() -> CheckUtil.checkNonNullContent(Arrays.asList("a", null, "b")))
      .isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  public void checkNonNullContent_notNull() {
    CheckUtil.checkNonNullContent(Arrays.asList("a", "c", "b"));
  }

  @Test
  public void checkType_null() {
    assertThatThrownBy(() -> CheckUtil.checkType(null, String.class))
      .isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  public void checkType_rightType() {
    CheckUtil.checkType(Collections.emptyList(), Collection.class);
  }

  @Test
  public void checkType_wrongType() {
    assertThatThrownBy(() -> CheckUtil.checkType(Collections.emptyList(), String.class))
      .isExactlyInstanceOf(ClassCastException.class)
      .hasMessage("Invalid type, expected : java.lang.String but was : java.util.Collections$EmptyList");
  }
}
