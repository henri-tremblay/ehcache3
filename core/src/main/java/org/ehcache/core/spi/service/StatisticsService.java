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

package org.ehcache.core.spi.service;

import org.ehcache.spi.service.Service;

/**
 * Service providing raw statistics for cache and tier usage.
 *
 * @author Henri Tremblay
 */
public interface StatisticsService extends Service {

  void clear(String cacheName);

  long getCacheHits(String cacheName);

  float getCacheHitPercentage(String cacheName);

  long getCacheMisses(String cacheName);

  float getCacheMissPercentage(String cacheName);

  long getCacheGets(String cacheName);

  long getCachePuts(String cacheName);

//  long getCacheUpdates(String cacheName);

  long getCacheRemovals(String cacheName);

  long getCacheEvictions(String cacheName);

//  long getCacheExpiry(String cacheName);

  float getCacheAverageGetTime(String cacheName);

  float getCacheAveragePutTime(String cacheName);

  float getCacheAverageRemoveTime(String cacheName);

//  long getCacheSize(String cacheName);

//  long getCacheCount(String cacheName);

  long getTierHits(String tierName);

  long getTierMisses(String tierName);

  long getTierEvictions(String tierName);

  long getTierMappings(String tierName);

  long getTierMaxMappings(String tierName);

  long getTierAllocatedByteSize(String tierName);

  long getTierOccupiedByteSize(String tierName);
}
