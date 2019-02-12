/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.boot.autoconfigure.embedded.cassandra;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ClassUtils;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.UrlFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandraAutoConfiguration}.
 *
 * @author Dmytro Nosan
 */
@EnabledOnJre(JRE.JAVA_8)
class EmbeddedCassandraAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@AfterEach
	void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	void setProperties() {
		load(ExcludeCassandraPostProcessor.class,
				"com.github.nosan.embedded.cassandra.working-directory=target/embedded-cassandra",
				"com.github.nosan.embedded.cassandra.artifact-directory=target/embedded-cassandra-artifact",
				"com.github.nosan.embedded.cassandra.version=3.11.3",
				"com.github.nosan.embedded.cassandra.allow-root=true",
				"com.github.nosan.embedded.cassandra.jmx-port=8999",
				"com.github.nosan.embedded.cassandra.register-shutdown-hook=true",
				"com.github.nosan.embedded.cassandra.startup-timeout=0s",
				"com.github.nosan.embedded.cassandra.configuration-file=classpath:cassandra.yaml",
				"com.github.nosan.embedded.cassandra.logback-file=classpath:logging.xml",
				"com.github.nosan.embedded.cassandra.topology-file=classpath:topology.properties",
				"com.github.nosan.embedded.cassandra.commit-log-archiving-file=" +
						"classpath:commitlog_archiving.properties",
				"com.github.nosan.embedded.cassandra.rack-file=classpath:rack.properties",
				"com.github.nosan.embedded.cassandra.jvm-options=-Dtest.property=property",
				"com.github.nosan.embedded.cassandra.java-home=target/java",
				"com.github.nosan.embedded.cassandra.artifact.directory=target/embedded-cassandra/artifact",
				"com.github.nosan.embedded.cassandra.artifact.url-factory=" +
						ClassUtils.getQualifiedName(MockUrlFactory.class),
				"com.github.nosan.embedded.cassandra.artifact.proxy.host=localhost",
				"com.github.nosan.embedded.cassandra.artifact.proxy.port=80",
				"com.github.nosan.embedded.cassandra.artifact.proxy.type=SOCKS",
				"com.github.nosan.embedded.cassandra.artifact.read-timeout=4s",
				"com.github.nosan.embedded.cassandra.artifact.connect-timeout=5s");

		assertThat(this.context.getBeansOfType(LocalCassandraFactory.class)).hasSize(1);
		LocalCassandraFactory factory = this.context.getBean(LocalCassandraFactory.class);

		assertThat(factory.getVersion()).isEqualTo(new Version(3, 11, 3));
		assertThat(factory.isRegisterShutdownHook()).isTrue();
		assertThat(factory.isAllowRoot()).isTrue();
		assertThat(factory.getJmxPort()).isEqualTo(8999);
		assertThat(factory.getJvmOptions()).containsExactly("-Dtest.property=property");
		assertThat(factory.getStartupTimeout()).isEqualTo(Duration.ZERO);
		assertThat(factory.getWorkingDirectory()).isEqualTo(Paths.get("target/embedded-cassandra"));
		assertThat(factory.getArtifactDirectory()).isEqualTo(Paths.get("target/embedded-cassandra-artifact"));
		assertThat(factory.getJavaHome()).isEqualTo(Paths.get("target/java"));
		assertThat(factory.getLogbackFile()).isNotNull().isEqualTo(getClass().getResource("/logging.xml"));
		assertThat(factory.getTopologyFile()).isNotNull().isEqualTo(getClass().getResource("/topology.properties"));
		assertThat(factory.getRackFile()).isNotNull().isEqualTo(getClass().getResource("/rack.properties"));
		assertThat(factory.getConfigurationFile()).isNotNull().isEqualTo(getClass().getResource("/cassandra.yaml"));
		assertThat(factory.getCommitLogArchivingFile()).isNotNull()
				.isEqualTo(getClass().getResource("/commitlog_archiving.properties"));
		assertThat(factory.getArtifactFactory()).isInstanceOf(RemoteArtifactFactory.class);
		RemoteArtifactFactory af = (RemoteArtifactFactory) factory.getArtifactFactory();
		assertThat(af).isNotNull();
		assertThat(af.getProxy()).isEqualTo(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 80)));
		assertThat(af.getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
		assertThat(af.getReadTimeout()).isEqualTo(Duration.ofSeconds(4));
		assertThat(af.getUrlFactory()).isInstanceOf(MockUrlFactory.class);
		assertThat(af.getDirectory()).isEqualTo(Paths.get("target/embedded-cassandra/artifact"));
	}

	@Test
	void propertiesAreAvailableInTheParentContext() {
		try (ConfigurableApplicationContext parent = new AnnotationConfigApplicationContext()) {
			parent.refresh();
			this.context = new AnnotationConfigApplicationContext();
			this.context.setParent(parent);
			this.context.register(EmbeddedCassandraAutoConfiguration.class,
					ClusterConfiguration.class);
			this.context.refresh();

			ConfigurableEnvironment environment = this.context.getEnvironment();
			String port = environment.getProperty("local.cassandra.port");
			String address = environment.getProperty("local.cassandra.address");
			assertThat(port).isNotNull();
			assertThat(address).isNotNull();
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	void clusterUseEmbeddedPortAndAddress() {
		load(ClusterConfiguration.class);

		ConfigurableEnvironment environment = this.context.getEnvironment();
		String port = environment.getProperty("local.cassandra.port");
		String address = environment.getProperty("local.cassandra.address");
		assertThat(port).isNotNull();
		assertThat(address).isNotNull();

		assertThat(this.context.getBeansOfType(Cluster.class)).hasSize(1);
		Cluster cluster = this.context.getBean(Cluster.class);

		Object manager = ReflectionTestUtils.getField(cluster, "manager");
		Object contactPoints = ReflectionTestUtils.getField(manager, "contactPoints");
		assertThat(contactPoints).isInstanceOf(Collection.class);
		assertThat((Collection) contactPoints).contains(new InetSocketAddress(address, Integer.parseInt(port)));

		try (Session session = cluster.connect()) {
			session.execute("SELECT now() FROM system.local;");
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	void clusterFactoryBeanEmbeddedPortAndAddress() {
		load(ClusterFactoryConfiguration.class);

		ConfigurableEnvironment environment = this.context.getEnvironment();
		String port = environment.getProperty("local.cassandra.port");
		String address = environment.getProperty("local.cassandra.address");
		assertThat(port).isNotNull();
		assertThat(address).isNotNull();

		assertThat(this.context.getBeansOfType(Cluster.class)).hasSize(1);
		Cluster cluster = this.context.getBean(Cluster.class);

		Object manager = ReflectionTestUtils.getField(cluster, "manager");
		Object contactPoints = ReflectionTestUtils.getField(manager, "contactPoints");
		assertThat(contactPoints).isInstanceOf(Collection.class);
		assertThat((Collection) contactPoints).contains(new InetSocketAddress(address, Integer.parseInt(port)));

		try (Session session = cluster.connect()) {
			session.execute("SELECT now() FROM system.local;");
		}
	}

	@Test
	void customCassandraBean() {
		load(CustomCassandraConfiguration.class);

		ConfigurableEnvironment environment = this.context.getEnvironment();
		String port = environment.getProperty("local.cassandra.port");
		String address = environment.getProperty("local.cassandra.address");
		assertThat(port).isNull();
		assertThat(address).isNull();

		assertThat(this.context.getBeansOfType(Cassandra.class)).hasSize(1);
		assertThat(this.context.getBeansOfType(Cluster.class)).hasSize(1);

		Cluster cluster = this.context.getBean(Cluster.class);
		try (Session session = cluster.connect()) {
			session.execute("SELECT now() FROM system.local;");
		}
	}

	private void load(Class<?> config, String... props) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		if (config != null) {
			ctx.register(config);
		}
		TestPropertyValues.of(props).applyTo(ctx);
		ctx.register(EmbeddedCassandraAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class);
		ctx.refresh();
		this.context = ctx;
	}

	@Configuration
	static class ClusterConfiguration {

		@Bean(destroyMethod = "close")
		public Cluster cluster(@Value("${local.cassandra.port}") int port,
				@Value("${local.cassandra.address}") String address) {
			return Cluster.builder().addContactPoint(address)
					.withoutMetrics().withoutJMXReporting()
					.withPort(port).build();
		}

	}

	@Configuration
	static class ClusterFactoryConfiguration {

		@Bean
		public CassandraClusterFactoryBean cluster(@Value("${local.cassandra.port}") int port,
				@Value("${local.cassandra.address}") String address) {
			CassandraClusterFactoryBean factoryBean = new CassandraClusterFactoryBean();
			factoryBean.setPort(port);
			factoryBean.setContactPoints(address);
			return factoryBean;
		}

	}

	@Configuration
	static class CustomCassandraConfiguration {

		@Bean(initMethod = "start", destroyMethod = "stop")
		public Cassandra embeddedCassandra() {
			return new LocalCassandraFactory().create();
		}

		@Bean(destroyMethod = "close")
		public Cluster cluster(Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			return Cluster.builder().addContactPoints(settings.getRealAddress())
					.withoutMetrics().withoutJMXReporting()
					.withPort(settings.getPort()).build();
		}
	}

	static class ExcludeCassandraPostProcessor implements BeanDefinitionRegistryPostProcessor {

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
			registry.removeBeanDefinition("embeddedCassandra");
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
				throws BeansException {
		}

	}

	static final class MockUrlFactory implements UrlFactory {

		@Override
		public URL[] create(Version version) {
			return new URL[0];
		}
	}
}
