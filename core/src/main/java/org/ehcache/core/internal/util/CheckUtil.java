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

import java.util.Collection;
import java.util.Objects;

/**
 * Functions common to assert parameter validity.
 */
public final class CheckUtil {

  private CheckUtil() {
  }

  public static void checkNonNull(Object thing) {
    Objects.requireNonNull(thing);
  }

  public static void checkNonNull(Object... things) {
    for (Object thing : things) {
      checkNonNull(thing);
    }
  }

  public static void checkNonNullContent(Collection<?> collectionOfThings) {
    checkNonNull(collectionOfThings);
    for (Object thing : collectionOfThings) {
      checkNonNull(thing);
    }
  }

  public static void checkType(Object object, Class<?> type) {
    if(!type.isAssignableFrom(Objects.requireNonNull(object.getClass()))) {
      throw new ClassCastException("Invalid type, expected : " + type.getName() + " but was : " + object.getClass().getName());
    }
  }
}
