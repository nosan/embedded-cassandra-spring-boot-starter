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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for an embedded {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({EmbeddedCassandraProperties.class, CassandraProperties.class})
@AutoConfigureBefore(CassandraAutoConfiguration.class)
@ConditionalOnClass({Cassandra.class, CassandraConnection.class})
@Import({CassandraConfiguration.class, CassandraConnectionConfiguration.class, CassandraClusterConfiguration.class})
public class EmbeddedCassandraAutoConfiguration {

	@Bean
	static CassandraInitializerBeanPostProcessor embeddedCassandraInitializerBeanPostProcessor(
			ApplicationContext applicationContext) {
		return new CassandraInitializerBeanPostProcessor(applicationContext);
	}

	@Bean
	@ConditionalOnSingleCandidate(CassandraConnection.class)
	CassandraInitializer embeddedCassandraInitializer(EmbeddedCassandraProperties properties,
			ApplicationContext applicationContext, @Lazy CassandraConnection cassandraConnection) {
		return new CassandraInitializer(properties, applicationContext, cassandraConnection);
	}

	/**
	 * Additional configuration to ensure that driver classes beans depend on {@link Cassandra} bean.
	 */
	@Configuration(proxyBeanMethods = false)
	@Conditional(OnAnyCassandraDriverCondition.class)
	static class CassandraDriversDependsOnEmbeddedCassandraConfiguration {

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
				catch (Exception ex) {
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

}
