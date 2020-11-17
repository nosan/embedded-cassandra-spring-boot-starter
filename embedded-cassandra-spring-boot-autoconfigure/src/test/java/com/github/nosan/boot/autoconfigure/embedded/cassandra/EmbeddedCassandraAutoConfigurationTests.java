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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;

import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * Tests for {@link EmbeddedCassandraAutoConfiguration}.
 *
 * @author Dmytro Nosan
 */
class EmbeddedCassandraAutoConfigurationTests {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(EmbeddedCassandraAutoConfiguration.class));

	@Test
	void userCqlSessionBean() {
		this.runner.withUserConfiguration(CqlSessionConfiguration.class)
				.run(context -> {
					CqlSession session = context.getBean(CqlSession.class);
					CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
				});
	}

	@Test
	void userCqlSessionFactoryBean() {
		this.runner.withUserConfiguration(CqlSessionFactoryBeanConfiguration.class)
				.run(context -> {
					CqlSession session = context.getBean(CqlSession.class);
					CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
				});
	}

	@Test
	void autoconfiguredCqlSessionBean() {
		this.runner.withConfiguration(AutoConfigurations.of(CassandraAutoConfiguration.class))
				.withPropertyValues("cassandra.embedded.version=4.0-alpha3")
				.withPropertyValues("cassandra.embedded.system-properties.cassandra.jmx.local.port=0")
				.withPropertyValues("spring.data.cassandra.local-datacenter=datacenter1")
				.run(context -> {
					CqlSession session = context.getBean(CqlSession.class);
					CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
				});
	}

	@Configuration(proxyBeanMethods = false)
	static class CqlSessionConfiguration {

		@Bean
		CqlSession cqlSession() {
			return new CqlSessionBuilder().build();
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CqlSessionFactoryBeanConfiguration {

		@Bean
		CqlSessionFactoryBean cqlSessionFactoryBean() {
			return new CqlSessionFactoryBean();
		}

	}

}
