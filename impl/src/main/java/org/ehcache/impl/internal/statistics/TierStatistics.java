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

package org.ehcache.impl.internal.statistics;

import org.ehcache.Cache;
import org.terracotta.context.ContextManager;
import org.terracotta.context.TreeNode;
import org.terracotta.context.query.Matcher;
import org.terracotta.context.query.Matchers;
import org.terracotta.context.query.Query;
import org.terracotta.statistics.ValueStatistic;

import java.util.Collections;
import java.util.Set;

import static org.terracotta.context.query.Matchers.attributes;
import static org.terracotta.context.query.Matchers.context;
import static org.terracotta.context.query.Matchers.hasAttribute;
import static org.terracotta.context.query.QueryBuilder.queryBuilder;

/**
 * Contains usage statistics relative to a given tier.
 *
 * @author Henri Tremblay
 */
class TierStatistics {

  private final ValueStatistic<Long> allocatedByteSize;
//  private final ValueStatistic<Long> hits;
//  private final ValueStatistic<Long> misses
//  private final ValueStatistic<Long> evictions;
//  private final ValueStatistic<Long> mappings;
//  private final ValueStatistic<Long> maxMappings;
//  private final ValueStatistic<Long> allocatedByteSize;
//  private final ValueStatistic<Long> occupiedByteSize;

  public TierStatistics(Cache<?, ?> cache, String tierName) {
    allocatedByteSize = findTierStatistic(cache, tierName, "allocatedBySize");
//    allocatedByteSize = findTierStatistic(cache, tierName, "allocatedBySize");
//    allocatedByteSize = findTierStatistic(cache, tierName, "allocatedBySize");
//    allocatedByteSize = findTierStatistic(cache, tierName, "allocatedBySize");
//    allocatedByteSize = findTierStatistic(cache, tierName, "allocatedBySize");
//    allocatedByteSize = findTierStatistic(cache, tierName, "allocatedBySize");
  }

  <T extends Number> ValueStatistic<T> findTierStatistic(Cache<?, ?> cache, final String tierName, String statName) {

    @SuppressWarnings("unchecked")
    Query statQuery = queryBuilder()
      .descendants()
      .filter(context(attributes(Matchers.allOf(
        hasAttribute("name", statName),
        hasAttribute("tags", new Matcher<Set<String>>() {
          @Override
          protected boolean matchesSafely(Set<String> object) {
            return object.contains(tierName);
          }
        })))))
      .build();

    Set<TreeNode> statResult = statQuery.execute(Collections.singleton(ContextManager.nodeFor(cache)));

    if (statResult.size() > 1) {
      throw new RuntimeException("One stat expected for " + statName + " but found " + statResult.size());
    }

    if (statResult.size() == 1) {
      @SuppressWarnings("unchecked")
      ValueStatistic<T> statistic = (ValueStatistic<T>) statResult.iterator().next().getContext().attributes().get("this");
      return statistic;
    }

    // No such stat in this tier
    return null;
  }


  public long getHits() {
    return 0;
  }

  public long getMisses() {
    return 0;
  }

  public long getEvictions() {
    return 0;
  }

  public long getMappings() {
    return 0;
  }

  public long getMaxMappings() {
    return 0;
  }

  public long getAllocatedByteSize() {
    return allocatedByteSize.value();
  }

  public long getOccupiedByteSize() {
    return 0;
  }
}
