/*
 * Copyright 2020-2021 the original author or authors.
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

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraBuilder;
import com.github.nosan.embedded.cassandra.CassandraBuilderConfigurator;
import com.github.nosan.embedded.cassandra.commons.UrlResource;
import com.github.nosan.embedded.cassandra.commons.logging.Logger;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for an embedded {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EmbeddedCassandraProperties.class)
@AutoConfigureBefore(CassandraAutoConfiguration.class)
@ConditionalOnClass(Cassandra.class)
public class EmbeddedCassandraAutoConfiguration {

	@Bean(initMethod = "start", destroyMethod = "stop")
	@ConditionalOnMissingBean
	Cassandra embeddedCassandra(CassandraBuilder embeddedCassandraBuilder) {
		return embeddedCassandraBuilder.build();
	}

	@Bean
	@ConditionalOnMissingBean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	CassandraBuilder embeddedCassandraBuilder(EmbeddedCassandraProperties properties,
			ObjectProvider<CassandraBuilderConfigurator> configurators) throws IOException {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		CassandraBuilder builder = new CassandraBuilder();
		builder.addEnvironmentVariables(properties.getEnvironmentVariables());
		builder.addSystemProperties(properties.getSystemProperties());
		builder.addConfigProperties(properties.getConfigProperties());
		builder.addJvmOptions(properties.getJvmOptions());
		map.from(properties::getRegisterShutdownHook).to(builder::registerShutdownHook);
		map.from(properties::getVersion).whenHasText().to(builder::version);
		map.from(properties::getLogger).whenHasText().as(Logger::get).to(builder::logger);
		map.from(properties::getName).whenHasText().to(builder::name);
		map.from(properties::getWorkingDirectory)
				.to(workingDirectory -> builder.workingDirectory(() -> workingDirectory));
		map.from(properties::getStartupTimeout).whenNot(Duration::isNegative).whenNot(Duration::isZero)
				.to(builder::startupTimeout);
		Resource configFile = properties.getConfigFile();
		if (configFile != null) {
			builder.configFile(new UrlResource(configFile.getURL()));
		}
		for (Map.Entry<String, Resource> entry : properties.getWorkingDirectoryResources().entrySet()) {
			builder.addWorkingDirectoryResource(new UrlResource(entry.getValue().getURL()), entry.getKey());
		}
		configurators.orderedStream().forEach(builder::configure);
		return builder;
	}

	/**
	 * Additional configuration to ensure that {@link CqlSession} beans depend on {@link Cassandra} bean.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(CqlSession.class)
	static class CqlSessionDependsOnEmbeddedCassandraConfiguration {

		@Bean
		static EmbeddedCassandraDependsOnBeanFactoryPostProcessor
		cqlSessionDependsOnEmbeddedCassandraBeanFactoryPostProcessor() {
			return new EmbeddedCassandraDependsOnBeanFactoryPostProcessor(CqlSession.class);
		}

		private static final class EmbeddedCassandraDependsOnBeanFactoryPostProcessor
				extends AbstractDependsOnBeanFactoryPostProcessor {

			EmbeddedCassandraDependsOnBeanFactoryPostProcessor(Class<?> beanClass) {
				super(beanClass, Cassandra.class);
			}

		}

	}

}
