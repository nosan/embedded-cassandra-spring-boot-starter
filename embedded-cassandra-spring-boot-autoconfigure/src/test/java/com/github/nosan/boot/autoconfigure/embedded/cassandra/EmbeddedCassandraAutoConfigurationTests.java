/*
 * Copyright 2020 the original author or authors.
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

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraBuilder;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.commons.UrlResource;
import com.github.nosan.embedded.cassandra.commons.logging.Logger;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandraAutoConfiguration}.
 *
 * @author Dmytro Nosan
 */
class EmbeddedCassandraAutoConfigurationTests {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(EmbeddedCassandraAutoConfiguration.class))
			.withPropertyValues("cassandra.embedded.startup-timeout=5m");

	@Test
	void configureProperties() {
		this.runner.withUserConfiguration(ExcludeCassandraBeanDefinitionRegistryPostProcessor.class)
				.withPropertyValues(
						"cassandra.embedded.config-file=classpath:cassandra.yaml",
						"cassandra.embedded.config-properties.start_rpc=true",
						"cassandra.embedded.environment-variables.JVM_OPTS=-Xmx512m",
						"cassandra.embedded.jvm-options=-Xmx256m",
						"cassandra.embedded.logger=MyLogger",
						"cassandra.embedded.version=3.11.3",
						"cassandra.embedded.name=MyCassandra",
						"cassandra.embedded.register-shutdown-hook=false",
						"cassandra.embedded.system-properties.cassandra.start_rpc=true",
						"cassandra.embedded.startup-timeout=1m",
						"cassandra.embedded.working-directory-resources.[conf/cassandra.yaml]=classpath:cassandra.yaml",
						"cassandra.embedded.working-directory=target/embeddedCassandra")
				.run(context -> {
					assertThat(context).hasSingleBean(CassandraBuilder.class);
					Cassandra cassandra = context.getBean(CassandraBuilder.class).build();
					assertThat(cassandra).hasFieldOrPropertyWithValue("databaseFactory.jvmOptions",
							Collections.singleton("-Xmx256m"));
					assertThat(cassandra).hasFieldOrPropertyWithValue("logger", Logger.get("MyLogger"));
					assertThat(cassandra).hasFieldOrPropertyWithValue("name", "MyCassandra");
					assertThat(cassandra).hasFieldOrPropertyWithValue("version", Version.parse("3.11.3"));
					assertThat(cassandra).hasFieldOrPropertyWithValue("registerShutdownHook", false);
					assertThat(cassandra).hasFieldOrPropertyWithValue("startupTimeout", Duration.ofMinutes(1));
					assertThat(cassandra).hasFieldOrPropertyWithValue("workingDirectory",
							Paths.get("target/embeddedCassandra").toAbsolutePath());
					assertThat(cassandra).hasFieldOrPropertyWithValue("databaseFactory.environmentVariables",
							Collections.singletonMap("JVM_OPTS", "-Xmx512m"));
					Map<String, Object> systemProperties = new LinkedHashMap<>();
					systemProperties.put("cassandra.start_rpc", "true");
					systemProperties.put("cassandra.config",
							new UrlResource(new ClassPathResource("cassandra.yaml").getURL()));
					assertThat(cassandra).hasFieldOrPropertyWithValue("databaseFactory.systemProperties",
							systemProperties);
					assertThat((Collection<?>) ReflectionTestUtils.getField(cassandra, "workingDirectoryCustomizers"))
							.hasSize(1);
				});
	}

	@Test
	void userCqlSessionBean() {
		this.runner.withUserConfiguration(CqlSessionConfiguration.class)
				.run(this::execute);
	}

	@Test
	void userCqlSessionFactoryBean() {
		this.runner.withUserConfiguration(CqlSessionFactoryBeanConfiguration.class)
				.run(this::execute);
	}

	@Test
	void autoconfiguredCqlSessionBean() {
		this.runner.withConfiguration(AutoConfigurations.of(CassandraAutoConfiguration.class))
				.withPropertyValues("spring.data.cassandra.local-datacenter=datacenter1")
				.run(this::execute);
	}

	private void execute(AssertableApplicationContext context) {
		CqlSession session = context.getBean(CqlSession.class);
		CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
	}

	@Configuration(proxyBeanMethods = false)
	static class CqlSessionConfiguration {

		@Bean
		CqlSession cqlSession() {
			return new CqlSessionBuilder().withLocalDatacenter("datacenter1")
					.withConfigLoader(DriverConfigLoader.programmaticBuilder()
							.withInt(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, 5000)
							.build())
					.build();
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CqlSessionFactoryBeanConfiguration {

		@Bean
		CqlSessionFactoryBean cqlSessionFactoryBean() {
			CqlSessionFactoryBean cqlSessionFactoryBean = new CqlSessionFactoryBean();
			cqlSessionFactoryBean.setLocalDatacenter("datacenter1");
			cqlSessionFactoryBean.setSessionBuilderConfigurer(
					sessionBuilder -> sessionBuilder.withConfigLoader(DriverConfigLoader.programmaticBuilder()
							.withInt(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, 5000)
							.build()));
			return cqlSessionFactoryBean;
		}

	}

	static class ExcludeCassandraBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory,
					Cassandra.class, true, false)) {
				((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(name);
			}

		}

	}

}
