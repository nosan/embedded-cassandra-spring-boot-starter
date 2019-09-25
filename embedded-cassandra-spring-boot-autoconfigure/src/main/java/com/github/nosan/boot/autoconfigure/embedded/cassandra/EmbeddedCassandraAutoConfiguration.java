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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;
import com.github.nosan.embedded.cassandra.artifact.Artifact;
import com.github.nosan.embedded.cassandra.commons.io.SpringResource;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for an embedded {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EmbeddedCassandraProperties.class)
@AutoConfigureBefore({CassandraAutoConfiguration.class, CassandraDataAutoConfiguration.class})
@ConditionalOnClass(Cassandra.class)
public class EmbeddedCassandraAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean(initMethod = "start", destroyMethod = "stop")
	public Cassandra embeddedCassandra(CassandraFactory embeddedCassandraFactory) {
		return embeddedCassandraFactory.create();
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(EmbeddedCassandraFactory.class)
	static class EmbeddedCassandraConfiguration {

		@Bean
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
			cassandraFactory.setExposeProperties(properties.isExposeProperties());

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

	/**
	 * Additional configuration to ensure that driver classes beans depend on {@link Cassandra} bean.
	 */
	@Configuration(proxyBeanMethods = false)
	@Conditional(EmbeddedCassandraDependsOnCondition.class)
	static class EmbeddedCassandraDependsOnConfiguration {

		/**
		 * Additional configuration to ensure that {@link CqlSession} bean depends on {@link Cassandra} bean.
		 */
		@Configuration(proxyBeanMethods = false)
		@ConditionalOnClass(CqlSession.class)
		static class EmbeddedCassandraCqlSessionDependsOnConfiguration {

			@Bean
			static CassandraDependsOnBeanFactoryPostProcessor cassandraCqlSessionDependsOnBeanFactoryPostProcessor() {
				return new CassandraDependsOnBeanFactoryPostProcessor(CqlSession.class, null);
			}

		}

		/**
		 * Additional configuration to ensure that {@link Cluster} and {@link Session} beans depend on {@link Cassandra}
		 * bean.
		 */
		@Configuration(proxyBeanMethods = false)
		@ConditionalOnClass(Cluster.class)
		static class EmbeddedCassandraClusterDependsOnConfiguration {

			@Bean
			static CassandraDependsOnBeanFactoryPostProcessor cassandraClusterDependsOnBeanFactoryPostProcessor() {
				return new CassandraDependsOnBeanFactoryPostProcessor(Cluster.class,
						getFactoryBeanClass("org.springframework.data.cassandra.config.CassandraClusterFactoryBean"));
			}

			@Bean
			static CassandraDependsOnBeanFactoryPostProcessor cassandraSessionDependsOnBeanFactoryPostProcessor() {
				return new CassandraDependsOnBeanFactoryPostProcessor(Session.class, getFactoryBeanClass(
						"org.springframework.data.cassandra.config.CassandraCqlSessionFactoryBean"));
			}

			@Nullable
			@SuppressWarnings("unchecked")
			private static Class<? extends FactoryBean<?>> getFactoryBeanClass(String name) {
				try {
					return (Class<? extends FactoryBean<?>>) ClassUtils.forName(name,
							EmbeddedCassandraClusterDependsOnConfiguration.class.getClassLoader());
				}
				catch (ClassNotFoundException ex) {
					return null;
				}
			}

		}

		private static class CassandraDependsOnBeanFactoryPostProcessor
				extends AbstractDependsOnBeanFactoryPostProcessor {

			CassandraDependsOnBeanFactoryPostProcessor(Class<?> beanClass,
					@Nullable Class<? extends FactoryBean<?>> factoryBeanClass) {
				super(beanClass, factoryBeanClass, Cassandra.class);
			}

		}

	}

	static class EmbeddedCassandraDependsOnCondition extends AnyNestedCondition {

		EmbeddedCassandraDependsOnCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnClass(Cluster.class)
		static class OnCluster {

		}

		@ConditionalOnClass(CqlSession.class)
		static class OnCqlSession {

		}

	}

}
