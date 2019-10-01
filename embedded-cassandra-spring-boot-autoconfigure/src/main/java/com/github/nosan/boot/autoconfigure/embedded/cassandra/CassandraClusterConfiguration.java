/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.boot.autoconfigure.embedded.cassandra;

import java.net.InetAddress;

import com.datastax.driver.core.Cluster;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.autoconfigure.cassandra.ClusterBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.github.nosan.embedded.cassandra.api.Cassandra;

/**
 * {@link Configuration @Configuration} that registers a {@link ClusterBuilderCustomizer} to set some {@link Cassandra}
 * properties.
 *
 * @author Dmytro Nosan
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnSingleCandidate(Cassandra.class)
@ConditionalOnClass(Cluster.class)
@ConditionalOnProperty(prefix = "com.github.nosan.embedded.cassandra", value = "configure-cluster",
		havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CassandraProperties.class)
class CassandraClusterConfiguration {

	@Bean
	@Order(Ordered.LOWEST_PRECEDENCE - 1)
	@Lazy
	ClusterBuilderCustomizer embeddedCassandraClusterBuilderCustomizer(Cassandra cassandra,
			CassandraProperties properties) {
		return clusterBuilder -> {
			InetAddress address = cassandra.getAddress();
			if (address != null) {
				clusterBuilder.addContactPoints(address);
			}
			int port = cassandra.getPort();
			if (port != -1) {
				clusterBuilder.withPort(port);
			}
			int sslPort = cassandra.getSslPort();
			if (properties.isSsl() && sslPort != -1) {
				clusterBuilder.withPort(sslPort);
			}
		};
	}

}
