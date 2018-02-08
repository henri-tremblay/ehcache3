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

package org.ehcache.clustered.spi;

import org.ehcache.CachePersistenceException;
import org.ehcache.clustered.client.config.ClusteredResourcePool;
import org.ehcache.clustered.client.config.ClusteringServiceConfiguration;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.clustered.client.internal.service.ClusteringServiceFactory;
import org.ehcache.clustered.client.internal.store.ClusteredStore;
import org.ehcache.clustered.client.internal.store.ClusteredStoreProviderFactory;
import org.ehcache.clustered.client.internal.store.ClusteredValueHolder;
import org.ehcache.clustered.client.service.ClusteringService;
import org.ehcache.clustered.util.KitManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.EvictionAdvisor;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.internal.service.ServiceLocator;
import org.ehcache.core.internal.store.StoreConfigurationImpl;
import org.ehcache.core.spi.store.Store;
import org.ehcache.core.spi.store.tiering.AuthoritativeTier;
import org.ehcache.core.spi.time.SystemTimeSource;
import org.ehcache.core.spi.time.TimeSource;
import org.ehcache.core.spi.time.TimeSourceService;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.impl.serialization.StringSerializer;
import org.ehcache.internal.store.StoreFactory;
import org.ehcache.internal.tier.AuthoritativeTierFactory;
import org.ehcache.internal.tier.AuthoritativeTierSPITest;
import org.ehcache.spi.service.Service;
import org.ehcache.spi.service.ServiceConfiguration;
import org.ehcache.spi.service.ServiceProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.terracotta.connection.Connection;
import org.terracotta.testing.rules.Cluster;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.terracotta.testing.rules.BasicExternalClusterBuilder.newCluster;

/**
 * ClusteredStoreSPITest
 */
public class ClusteredStoreSPITest extends AuthoritativeTierSPITest<String, String> {

  private static final boolean FORCE_KIT_REFRESH = false;

  static {
    KitManager.initInstallationPath(FORCE_KIT_REFRESH);
  }

  private static final String CACHE_IDENTIFIER = "testCache";
  private static final String RESOURCE_CONFIG =
    "<config xmlns:ohr='http://www.terracotta.org/config/offheap-resource'>"
    + "<ohr:offheap-resources>"
    + "<ohr:resource name=\"primary\" unit=\"MB\">64</ohr:resource>"
    + "</ohr:offheap-resources>" +
    "</config>\n";

  private static ClusteredStore.Provider provider;

  @ClassRule
  public static Cluster CLUSTER =
    newCluster().in(new File("build/cluster")).withServiceFragment(RESOURCE_CONFIG).build();
  private static Connection CONNECTION;

  @BeforeClass
  public static void waitForActive() throws Exception {
    CLUSTER.getClusterControl().waitForActive();
    CONNECTION = CLUSTER.newConnection();
  }

  @AfterClass
  public static void closeConnection() throws IOException {
    CONNECTION.close();
  }

  private AuthoritativeTierFactory<String, String> authoritativeTierFactory;

  @Before
  public void setUp() {
    ClusteringServiceConfiguration clusteringServiceConfiguration =
      ClusteringServiceConfigurationBuilder
        .cluster(CLUSTER.getConnectionURI().resolve("/TestCacheManager"))
        .autoCreate()
        .defaultServerResource("primary")
        .build();
    ClusteringService clusteringService = new ClusteringServiceFactory()
      .create(clusteringServiceConfiguration);

    ServiceProvider<Service> serviceLocator = mock(ServiceProvider.class);
    when(serviceLocator.getService(ClusteringService.class)).thenReturn(clusteringService);

    ClusteredStoreProviderFactory storeProviderFactory = new ClusteredStoreProviderFactory();
    provider = storeProviderFactory.create(null);
    provider.start(serviceLocator);

    authoritativeTierFactory = new AuthoritativeTierFactory<String, String>() {

      @Override
      public AuthoritativeTier<String, String> newStore() {
        return newStore(null, null, ExpiryPolicyBuilder.noExpiration(), SystemTimeSource.INSTANCE);
      }

      @Override
      public AuthoritativeTier<String, String> newStoreWithCapacity(long capacity) {
        return newStore(capacity, null, ExpiryPolicyBuilder.noExpiration(), SystemTimeSource.INSTANCE);
      }

      @Override
      public AuthoritativeTier<String, String> newStoreWithExpiry(ExpiryPolicy<? super String, ? super String> expiry, TimeSource timeSource) {
        return newStore(null, null, expiry, timeSource);
      }

      @Override
      public AuthoritativeTier<String, String> newStoreWithEvictionAdvisor(EvictionAdvisor<String, String> evictionAdvisor) {
        return newStore(null, evictionAdvisor, ExpiryPolicyBuilder.noExpiration(), SystemTimeSource.INSTANCE);
      }

      private AuthoritativeTier<String, String> newStore(Long capacity, EvictionAdvisor<String, String> evictionAdvisor, ExpiryPolicy<? super String, ? super String> expiry, TimeSource timeSource) {
        TimeSourceService timeSourceService = mock(TimeSourceService.class);
        when(timeSourceService.getTimeSource()).thenReturn(timeSource);
        when(serviceLocator.getService(TimeSourceService.class)).thenReturn(timeSourceService);

        ClusteredResourcePool resourcePool = ClusteredResourcePoolBuilder.clusteredDedicated(4, MemoryUnit.MB);
        ResourcePools resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
          .with(resourcePool)
          .build();

        Store.Configuration config = new StoreConfigurationImpl(
          getKeyType(),
          getValueType(),
          evictionAdvisor,
          getClass().getClassLoader(),
          expiry,
          resourcePools,
          1,
          new StringSerializer(),
          new StringSerializer()
        );

        ClusteredStore<String, String> store = provider.createStore(config, getServiceConfigurations());
        provider.initStore(store);

        return store;
      }

      @Override
      public Store.ValueHolder<String> newValueHolder(String value) {
        return new ClusteredValueHolder<>(value);
      }

      @Override
      public Class<String> getKeyType() {
        return String.class;
      }

      @Override
      public Class<String> getValueType() {
        return String.class;
      }

      @Override
      public ServiceConfiguration<?>[] getServiceConfigurations() {
        ClusteredResourcePool resourcePool = ClusteredResourcePoolBuilder.clusteredDedicated(4, MemoryUnit.MB);
        ResourcePools resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
          .with(resourcePool)
          .build();

        CacheConfiguration<String, String> cacheConfiguration = CacheConfigurationBuilder
          .newCacheConfigurationBuilder(getKeyType(), getValueType(), resourcePools)
          .build();

        ServiceConfiguration<?> id;
        try {
          id = clusteringService.getPersistenceSpaceIdentifier(CACHE_IDENTIFIER, cacheConfiguration);
        } catch (CachePersistenceException e) {
          throw new RuntimeException(e);
        }

        clusteringService.start(serviceLocator);

        return new ServiceConfiguration[] { id };
      }

      @Override
      public ServiceLocator getServiceProvider() {
        return null;
      }

      @Override
      public String createKey(long seed) {
        return Long.toString(seed);
      }

      @Override
      public String createValue(long seed) {
        char[] chars = new char[600 * 1024];
        Arrays.fill(chars, (char) (0x1 + (seed & 0x7e)));
        return new String(chars);
      }

      @Override
      public void close(final Store<String, String> store) {
        provider.releaseStore(store);
      }
    };
  }

  @Override
  protected AuthoritativeTierFactory<String, String> getAuthoritativeTierFactory() {
    return authoritativeTierFactory;
  }

  @Override
  protected StoreFactory<String, String> getStoreFactory() {
    return getAuthoritativeTierFactory();
  }

}
