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

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.time.Duration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.ClusterBuilderCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.artifact.Artifact;
import com.github.nosan.embedded.cassandra.artifact.RemoteArtifact;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandraAutoConfiguration}.
 *
 * @author Dmytro Nosan
 */
@EnabledOnJre(JRE.JAVA_8)
class EmbeddedCassandraAutoConfigurationTests {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(EmbeddedCassandraAutoConfiguration.class));

	@Test
	void configureProperties() {
		this.runner.withUserConfiguration(ExcludeCassandraBeanDefinitionRegistryPostProcessor.class)
				.withPropertyValues("com.github.nosan.embedded.cassandra.config=classpath:cassandra.yaml",
						"com.github.nosan.embedded.cassandra.config-properties.start_rpc=true",
						"com.github.nosan.embedded.cassandra.daemon=true",
						"com.github.nosan.embedded.cassandra.environment-variables.JVM_OPTS=-Xmx512m",
						"com.github.nosan.embedded.cassandra.java-home=target/java",
						"com.github.nosan.embedded.cassandra.jmx-local-port=7199",
						"com.github.nosan.embedded.cassandra.jvm-options=-Xmx256m",
						"com.github.nosan.embedded.cassandra.logger=MyLogger",
						"com.github.nosan.embedded.cassandra.version=3.11.3",
						"com.github.nosan.embedded.cassandra.name=MyCassandra",
						"com.github.nosan.embedded.cassandra.port=9042",
						"com.github.nosan.embedded.cassandra.rack-config=classpath:cassandra-rack.properties",
						"com.github.nosan.embedded.cassandra.register-shutdown-hook=true",
						"com.github.nosan.embedded.cassandra.root-allowed=false",
						"com.github.nosan.embedded.cassandra.rpc-port=9160",
						"com.github.nosan.embedded.cassandra.ssl-port=9142",
						"com.github.nosan.embedded.cassandra.ssl-storage-port=7001",
						"com.github.nosan.embedded.cassandra.storage-port=7000",
						"com.github.nosan.embedded.cassandra.system-properties.[cassandra.start_rpc]=true",
						"com.github.nosan.embedded.cassandra.timeout=1m",
						"com.github.nosan.embedded.cassandra.topology-config=classpath:cassandra-topology.properties",
						"com.github.nosan.embedded.cassandra.working-directory=target/embeddedCassandra")
				.run(context -> {
					assertThat(context).hasSingleBean(CassandraFactory.class)
							.getBean(CassandraFactory.class).isInstanceOf(EmbeddedCassandraFactory.class);
					EmbeddedCassandraFactory cassandraFactory = context.getBean(EmbeddedCassandraFactory.class);
					assertThat(cassandraFactory.getConfig()).extracting(Resource::getFileName).isEqualTo(
							"cassandra.yaml");
					assertThat(cassandraFactory.getConfigProperties()).containsEntry("start_rpc", "true");
					assertThat(cassandraFactory.isDaemon()).isTrue();
					assertThat(cassandraFactory.isRegisterShutdownHook()).isTrue();
					assertThat(cassandraFactory.isRootAllowed()).isFalse();
					assertThat(cassandraFactory.getEnvironmentVariables()).containsEntry("JVM_OPTS", "-Xmx512m");
					assertThat(cassandraFactory.getSystemProperties()).containsEntry("cassandra.start_rpc", "true");
					assertThat(cassandraFactory.getJavaHome()).isEqualTo(Paths.get("target/java"));
					assertThat(cassandraFactory.getWorkingDirectory()).isEqualTo(Paths.get("target/embeddedCassandra"));
					assertThat(cassandraFactory.getJmxLocalPort()).isEqualTo(7199);
					assertThat(cassandraFactory.getRpcPort()).isEqualTo(9160);
					assertThat(cassandraFactory.getStoragePort()).isEqualTo(7000);
					assertThat(cassandraFactory.getSslStoragePort()).isEqualTo(7001);
					assertThat(cassandraFactory.getSslPort()).isEqualTo(9142);
					assertThat(cassandraFactory.getPort()).isEqualTo(9042);
					assertThat(cassandraFactory.getTimeout()).isEqualTo(Duration.ofMinutes(1));
					assertThat(cassandraFactory.getJvmOptions()).contains("-Xmx256m");
					assertThat(cassandraFactory.getLogger()).isEqualTo(LoggerFactory.getLogger("MyLogger"));
					Artifact artifact = cassandraFactory.getArtifact();
					assertThat(artifact).isInstanceOf(RemoteArtifact.class);
					assertThat(((RemoteArtifact) artifact)).extracting(RemoteArtifact::getVersion).isEqualTo(
							Version.of("3.11.3"));
					assertThat(cassandraFactory.getName()).isEqualTo("MyCassandra");
					assertThat(cassandraFactory.getRackConfig()).extracting(Resource::getFileName).isEqualTo(
							"cassandra-rack.properties");
					assertThat(cassandraFactory.getTopologyConfig()).extracting(Resource::getFileName).isEqualTo(
							"cassandra-topology.properties");

				});

	}

	@Test
	void usingAutoConfiguredCluster() {
		this.runner.withConfiguration(AutoConfigurations.of(CassandraAutoConfiguration.class))
				.withPropertyValues("com.github.nosan.embedded.cassandra.scripts=classpath:schema.cql",
						"spring.data.cassandra.keyspace-name=test")
				.run(context -> context.getBean(Cluster.class).connect("test"));
	}

	@Test
	void disableClusterConfiguration() {
		this.runner.withPropertyValues("com.github.nosan.embedded.cassandra.configure-cluster=false")
				.run(context -> assertThat(context).doesNotHaveBean(ClusterBuilderCustomizer.class));
	}

	@Test
	void configureCqlScripts() {
		this.runner.withUserConfiguration(ClusterConfiguration.class)
				.withPropertyValues("com.github.nosan.embedded.cassandra.scripts=classpath:schema.cql",
						"spring.data.cassandra.keyspace-name=test")
				.run(context -> context.getBean(Cluster.class).connect("test"));
	}

	@Test
	void configureEmbeddedConnection() {
		this.runner.run(context -> {
			assertThat(context).hasSingleBean(Cassandra.class).hasSingleBean(CassandraConnection.class);
			CassandraConnection connection = context.getBean(CassandraConnection.class);
			connection.execute("SELECT now() FROM system.local;");
		});
	}

	@Test
	void usingClusterBean() {
		this.runner.withUserConfiguration(ClusterConfiguration.class).run(context -> {
			assertThat(context).hasSingleBean(Cassandra.class).hasSingleBean(Cluster.class);
			Cluster cluster = context.getBean(Cluster.class);
			try (Session session = cluster.connect()) {
				session.execute("SELECT now() FROM system.local;");
			}
		});
	}

	@Test
	void usingCqlSessionBean() {
		this.runner.withUserConfiguration(CqlSessionConfiguration.class).run(context -> {
			assertThat(context).hasSingleBean(Cassandra.class).hasSingleBean(CqlSession.class);
			CqlSession session = context.getBean(CqlSession.class);
			session.execute("SELECT now() FROM system.local;");
		});
	}

	@Test
	void usingClusterFactoryBean() {
		this.runner.withUserConfiguration(ClusterFactoryBeanConfiguration.class).run(context -> {
			assertThat(context).hasSingleBean(Cassandra.class).hasSingleBean(Cluster.class);
			Cluster cluster = context.getBean(Cluster.class);
			try (Session session = cluster.connect()) {
				session.execute("SELECT now() FROM system.local;");
			}
		});
	}

	@Test
	void usingCustomCassandraFactory() {
		this.runner.withUserConfiguration(CustomCassandraFactoryConfiguration.class,
				ClusterConfiguration.class).run(context -> {
			assertThat(context).hasSingleBean(Cassandra.class).hasSingleBean(CassandraFactory.class).hasSingleBean(
					Cluster.class).hasBean("customCassandraFactory");
			assertThat(context.getBean("customCassandraFactory")).isInstanceOf(CassandraFactory.class);
			Cluster cluster = context.getBean(Cluster.class);
			try (Session session = cluster.connect()) {
				session.execute("SELECT now() FROM system.local;");
			}
		});
	}

	@Test
	void usingCassandraFactoryCustomizers() {
		this.runner.withUserConfiguration(ExcludeCassandraBeanDefinitionRegistryPostProcessor.class)
				.withBean(CustomCassandraFactoryCustomizerConfiguration.class)
				.run(context -> {
					assertThat(context).hasSingleBean(CassandraFactory.class).getBean(CassandraFactory.class)
							.isInstanceOf(EmbeddedCassandraFactory.class);
					EmbeddedCassandraFactory cassandraFactory = context.getBean(EmbeddedCassandraFactory.class);
					assertThat(cassandraFactory.getName()).isEqualTo("customCassandra");
				});
	}

	@Configuration(proxyBeanMethods = false)
	static class ClusterConfiguration {

		@Bean(destroyMethod = "close")
		Cluster cluster(Cassandra cassandra) {
			return Cluster.builder().addContactPoints(cassandra.getAddress()).withoutMetrics().withoutJMXReporting()
					.withPort(cassandra.getPort()).build();
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CqlSessionConfiguration {

		@Bean(destroyMethod = "close")
		CqlSession cqlSession(Cassandra cassandra) {
			return CqlSession.builder().withLocalDatacenter("datacenter1").addContactPoint(
					new InetSocketAddress(cassandra.getAddress(), cassandra.getPort())).build();
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class ClusterFactoryBeanConfiguration {

		@Bean
		CassandraClusterFactoryBean cluster(Cassandra cassandra) {
			CassandraClusterFactoryBean factoryBean = new CassandraClusterFactoryBean();
			factoryBean.setMetricsEnabled(false);
			factoryBean.setJmxReportingEnabled(false);
			factoryBean.setPort(cassandra.getPort());
			factoryBean.setContactPoints(cassandra.getAddress().getHostAddress());
			return factoryBean;
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CustomCassandraFactoryConfiguration {

		@Bean
		CassandraFactory customCassandraFactory() {
			return new EmbeddedCassandraFactory();
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CustomCassandraFactoryCustomizerConfiguration {

		@Bean
		CassandraFactoryCustomizer<EmbeddedCassandraFactory> nameCassandraFactoryCustomizer() {
			return cassandraFactory -> cassandraFactory.setName("customCassandra");
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
