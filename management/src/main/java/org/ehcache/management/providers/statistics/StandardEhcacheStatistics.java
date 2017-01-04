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
package org.ehcache.management.providers.statistics;

import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.statistics.TierOperationOutcomes;
import org.ehcache.management.ManagementRegistryServiceConfiguration;
import org.ehcache.management.config.StatisticsProviderConfiguration;
import org.ehcache.management.providers.CacheBinding;
import org.ehcache.management.providers.ExposedCacheBinding;
import org.terracotta.context.extended.OperationStatisticDescriptor;
import org.terracotta.context.extended.StatisticsRegistry;
import org.terracotta.management.model.capabilities.descriptors.StatisticDescriptor;
import org.terracotta.management.model.stats.NumberUnit;
import org.terracotta.management.model.stats.Sample;
import org.terracotta.management.model.stats.Statistic;
import org.terracotta.management.model.stats.StatisticType;
import org.terracotta.management.model.stats.history.CounterHistory;
import org.terracotta.management.registry.collect.StatisticsRegistryMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singleton;
import static java.util.EnumSet.allOf;
import static java.util.EnumSet.of;
import static org.terracotta.context.extended.ValueStatisticDescriptor.descriptor;

public class StandardEhcacheStatistics extends ExposedCacheBinding {

  private final StatisticsRegistry statisticsRegistry;
  private final StatisticsRegistryMetadata statisticsRegistryMetadata;
  private final StatisticsService statisticsService;
  private final String cacheName;

  StandardEhcacheStatistics(ManagementRegistryServiceConfiguration registryConfiguration, CacheBinding cacheBinding,
                            StatisticsProviderConfiguration statisticsProviderConfiguration, ScheduledExecutorService executor,
                            StatisticsService statisticsService) {
    super(registryConfiguration, cacheBinding);
    this.cacheName = cacheBinding.getAlias();
    this.statisticsRegistry = new StatisticsRegistry(cacheBinding.getCache(), executor, statisticsProviderConfiguration.averageWindowDuration(),
        statisticsProviderConfiguration.averageWindowUnit(), statisticsProviderConfiguration.historySize(), 5, TimeUnit.SECONDS,
        statisticsProviderConfiguration.timeToDisable(), statisticsProviderConfiguration.timeToDisableUnit());
    this.statisticsRegistryMetadata = new StatisticsRegistryMetadata(statisticsRegistry);
    this.statisticsService = statisticsService;

    Class<TierOperationOutcomes.GetOutcome> tierOperationGetOucomeClass = TierOperationOutcomes.GetOutcome.class;
    OperationStatisticDescriptor<TierOperationOutcomes.GetOutcome> getTierStatisticDescriptor = OperationStatisticDescriptor.descriptor("get", singleton("tier"), tierOperationGetOucomeClass);

    statisticsRegistry.registerCompoundOperations("Hit", getTierStatisticDescriptor, of(TierOperationOutcomes.GetOutcome.HIT));
    statisticsRegistry.registerCompoundOperations("Miss", getTierStatisticDescriptor, of(TierOperationOutcomes.GetOutcome.MISS));
    statisticsRegistry.registerCompoundOperations("Eviction",
        OperationStatisticDescriptor.descriptor("eviction", singleton("tier"),
        TierOperationOutcomes.EvictionOutcome.class),
        allOf(TierOperationOutcomes.EvictionOutcome.class));
    statisticsRegistry.registerCounter("MappingCount", descriptor("mappings", singleton("tier")));
    statisticsRegistry.registerCounter("MaxMappingCount", descriptor("maxMappings", singleton("tier")));
    statisticsRegistry.registerSize("AllocatedByteSize", descriptor("allocatedMemory", singleton("tier")));
    statisticsRegistry.registerSize("OccupiedByteSize", descriptor("occupiedMemory", singleton("tier")));
  }

  public Statistic<?, ?> queryStatistic(String fullStatisticName, long since) {
    if ("Cache:HitCount".equals(fullStatisticName)) {
      return createStatistic(statisticsService.getCacheHits(cacheName));
    }
    if ("Cache:MissCount".equals(fullStatisticName)) {
      return createStatistic(statisticsService.getCacheMisses(cacheName));
    }
    if ("Disk:AllocatedByteSize".equals(fullStatisticName)) {
      return createStatistic(statisticsService.getTierAllocatedByteSize("Disk"));
    }
    return statisticsRegistryMetadata.queryStatistic(fullStatisticName, since);
  }

  @Override
  public Collection<StatisticDescriptor> getDescriptors() {
    Collection<? extends StatisticDescriptor> coll = statisticsRegistryMetadata.getDescriptors();
    Collection<StatisticDescriptor> result = new ArrayList<StatisticDescriptor>(coll.size() + 2);
    result.add(new StatisticDescriptor("Cache:HitCount", StatisticType.COUNTER));
    result.add(new StatisticDescriptor("Cache:MissCount", StatisticType.COUNTER));
    return result;
  }

  void dispose() {
    statisticsRegistry.clearRegistrations();
  }

  private Statistic<?, ?> createStatistic(long count) {
    long now = System.currentTimeMillis();
    return new CounterHistory(NumberUnit.COUNT, new Sample[] {
      new Sample(now - 0, count)
    });
  }
}
