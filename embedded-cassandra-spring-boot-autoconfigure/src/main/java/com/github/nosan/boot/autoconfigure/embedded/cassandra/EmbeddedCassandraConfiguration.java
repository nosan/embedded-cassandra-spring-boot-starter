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
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;
import com.github.nosan.embedded.cassandra.artifact.Artifact;
import com.github.nosan.embedded.cassandra.commons.io.SpringResource;

/**
 * {@link Configuration @Configuration} for an embedded {@link Cassandra}.
 *
 * @author Dmytro Nosan
 */
@Configuration(proxyBeanMethods = false)
class EmbeddedCassandraConfiguration {

	@Bean(destroyMethod = "stop")
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(CassandraFactory.class)
	Cassandra embeddedCassandra(CassandraFactory embeddedCassandraFactory, ApplicationContext applicationContext,
			EmbeddedCassandraProperties cassandraProperties) {
		Cassandra cassandra = embeddedCassandraFactory.create();
		cassandra.start();
		if (cassandraProperties.isExposeProperties()) {
			Map<String, Object> properties = new LinkedHashMap<>();
			InetAddress address = cassandra.getAddress();
			if (address != null) {
				properties.put("embedded.cassandra.address", address.getHostAddress());
			}
			int port = cassandra.getPort();
			if (port != -1) {
				properties.put("embedded.cassandra.port", port);
			}
			int sslPort = cassandra.getSslPort();
			if (sslPort != -1) {
				properties.put("embedded.cassandra.ssl-port", sslPort);
			}
			int rpcPort = cassandra.getRpcPort();
			if (rpcPort != -1) {
				properties.put("embedded.cassandra.rpc-port", rpcPort);
			}
			properties.put("embedded.cassandra.version", cassandra.getVersion().toString());
			setProperties(applicationContext, properties);
		}
		return cassandra;
	}

	private static void setProperties(ApplicationContext applicationContext, Map<String, Object> properties) {
		if (applicationContext instanceof ConfigurableApplicationContext) {
			MutablePropertySources propertySources = ((ConfigurableApplicationContext) applicationContext)
					.getEnvironment().getPropertySources();
			getProperties(propertySources).putAll(properties);
		}
		if (applicationContext.getParent() != null) {
			setProperties(applicationContext.getParent(), properties);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> getProperties(MutablePropertySources sources) {
		PropertySource<?> propertySource = sources.get("embeddedCassandra");
		if (propertySource == null) {
			propertySource = new MapPropertySource("embeddedCassandra", new LinkedHashMap<>());
			sources.addFirst(propertySource);
		}
		return (Map<String, Object>) propertySource.getSource();
	}

	/**
	 * {@link Configuration} for {@link EmbeddedCassandraFactory}.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({EmbeddedCassandraFactory.class, Logger.class, Yaml.class, ArchiveEntry.class})
	static class EmbeddedCassandraFactoryConfiguration {

		@Bean
		@Scope("prototype")
		@ConditionalOnMissingBean
		CassandraFactory embeddedCassandraFactory(EmbeddedCassandraProperties properties,
				ObjectProvider<CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>> customizers) {

			PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();

			EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
			cassandraFactory.getEnvironmentVariables().putAll(properties.getEnvironmentVariables());
			cassandraFactory.getSystemProperties().putAll(properties.getSystemProperties());
			cassandraFactory.getConfigProperties().putAll(properties.getConfigProperties());
			cassandraFactory.getJvmOptions().addAll(properties.getJvmOptions());
			cassandraFactory.setDaemon(properties.isDaemon());
			cassandraFactory.setRegisterShutdownHook(properties.isRegisterShutdownHook());
			cassandraFactory.setRootAllowed(properties.isRootAllowed());

			map.from(properties::getVersion).whenHasText().as(Artifact::ofVersion).to(cassandraFactory::setArtifact);
			map.from(properties::getLogger).whenHasText().as(LoggerFactory::getLogger).to(cassandraFactory::setLogger);
			map.from(properties::getName).whenHasText().to(cassandraFactory::setName);

			map.from(properties::getJavaHome).to(cassandraFactory::setJavaHome);
			map.from(properties::getWorkingDirectory).to(cassandraFactory::setWorkingDirectory);

			map.from(properties::getTimeout).whenNot(Duration::isNegative).whenNot(Duration::isZero)
					.to(cassandraFactory::setTimeout);

			map.from(properties::getPort).to(cassandraFactory::setPort);
			map.from(properties::getSslPort).to(cassandraFactory::setSslPort);
			map.from(properties::getRpcPort).to(cassandraFactory::setRpcPort);
			map.from(properties::getStoragePort).to(cassandraFactory::setStoragePort);
			map.from(properties::getSslStoragePort).to(cassandraFactory::setSslStoragePort);
			map.from(properties::getJmxLocalPort).to(cassandraFactory::setJmxLocalPort);

			map.from(properties::getConfig).as(SpringResource::new).to(cassandraFactory::setConfig);
			map.from(properties::getTopologyConfig).as(SpringResource::new).to(cassandraFactory::setTopologyConfig);
			map.from(properties::getRackConfig).as(SpringResource::new).to(cassandraFactory::setRackConfig);

			customizers.orderedStream().forEach(customizer -> customizer.customize(cassandraFactory));

			return cassandraFactory;
		}

	}

}
