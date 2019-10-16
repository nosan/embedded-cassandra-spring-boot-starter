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

import java.time.Duration;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
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
class CassandraConfiguration {

	@Bean(initMethod = "start", destroyMethod = "stop")
	@ConditionalOnSingleCandidate(CassandraFactory.class)
	@ConditionalOnMissingBean
	Cassandra embeddedCassandra(CassandraFactory cassandraFactory) {
		return cassandraFactory.create();
	}

	/**
	 * {@link Configuration} for {@link EmbeddedCassandraFactory}.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({EmbeddedCassandraFactory.class, Logger.class, Yaml.class, ArchiveEntry.class})
	static class CassandraFactoryConfiguration {

		@Bean
		@Scope(BeanDefinition.SCOPE_PROTOTYPE)
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
			map.from(properties::getAddress).to(cassandraFactory::setAddress);

			map.from(properties::getConfig).as(SpringResource::new).to(cassandraFactory::setConfig);
			map.from(properties::getTopologyConfig).as(SpringResource::new).to(cassandraFactory::setTopologyConfig);
			map.from(properties::getRackConfig).as(SpringResource::new).to(cassandraFactory::setRackConfig);

			customizers.orderedStream().forEach(customizer -> customizer.customize(cassandraFactory));

			return cassandraFactory;
		}

	}

}
