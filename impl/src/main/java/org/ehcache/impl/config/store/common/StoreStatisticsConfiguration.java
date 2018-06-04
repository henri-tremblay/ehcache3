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

package org.ehcache.impl.config.store.common;

import org.ehcache.core.spi.store.Store;
import org.ehcache.spi.service.ServiceConfiguration;

/**
 * Common configuration for all stores.
 */
public class StoreStatisticsConfiguration implements ServiceConfiguration<Store.Provider> {

  private final boolean enableStatistics;

  /**
   * Default constructor.
   *
   * @param enableStatistics if statistics should be enabled on stores
   */
  public StoreStatisticsConfiguration(boolean enableStatistics) {
    this.enableStatistics = enableStatistics;
  }

  public boolean isEnableStatistics() {
    return enableStatistics;
  }

  @Override
  public Class<Store.Provider> getServiceType() {
    return Store.Provider.class;
  }
}
