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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for an embedded {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EmbeddedCassandraProperties.class)
@AutoConfigureBefore(CassandraAutoConfiguration.class)
@ConditionalOnClass({Cassandra.class, CassandraConnection.class})
@Import({EmbeddedCassandraConfiguration.class, EmbeddedCassandraConnectionConfiguration.class})
public class EmbeddedCassandraAutoConfiguration {

	@Bean
	static EmbeddedCassandraInitializerBeanPostProcessor embeddedCassandraInitializerBeanPostProcessor(
			ApplicationContext applicationContext) {
		return new EmbeddedCassandraInitializerBeanPostProcessor(applicationContext);
	}

	@Bean
	EmbeddedCassandraInitializer embeddedCassandraInitializer(ObjectProvider<CassandraConnection> cassandraConnections,
			ObjectProvider<Cassandra> cassandras, EmbeddedCassandraProperties properties,
			ApplicationContext applicationContext) {
		return new EmbeddedCassandraInitializer(properties, applicationContext, cassandraConnections, cassandras);
	}

}
