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

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.util.ClassUtils;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.WorkingDirectoryCustomizer;
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
	void configureProperties() {
		load(ExcludeCassandraPostProcessor.class,
				"com.github.nosan.embedded.cassandra.working-directory=target/embedded-cassandra",
				"com.github.nosan.embedded.cassandra.artifact-directory=target/embedded-cassandra-artifact",
				"com.github.nosan.embedded.cassandra.version=3.11.3",
				"com.github.nosan.embedded.cassandra.startup-timeout=30S",
				"com.github.nosan.embedded.cassandra.daemon=false",
				"com.github.nosan.embedded.cassandra.allow-root=true",
				"com.github.nosan.embedded.cassandra.jmx-local-port=7199",
				"com.github.nosan.embedded.cassandra.port=9042",
				"com.github.nosan.embedded.cassandra.storage-port=7000",
				"com.github.nosan.embedded.cassandra.ssl-storage-port=7001",
				"com.github.nosan.embedded.cassandra.rpc-port=9160",
				"com.github.nosan.embedded.cassandra.register-shutdown-hook=true",
				"com.github.nosan.embedded.cassandra.delete-working-directory=true",
				"com.github.nosan.embedded.cassandra.configuration-file=classpath:cassandra.yaml",
				"com.github.nosan.embedded.cassandra.logging-file=classpath:logging.xml",
				"com.github.nosan.embedded.cassandra.topology-file=classpath:topology.properties",
				"com.github.nosan.embedded.cassandra.rack-file=classpath:rack.properties",
				"com.github.nosan.embedded.cassandra.jvm-options=-Dtest.property=property",
				"com.github.nosan.embedded.cassandra.environment-variables.NAME=cassandra",
				"com.github.nosan.embedded.cassandra.java-home=target/java",
				"com.github.nosan.embedded.cassandra.artifact.url-factory=" + ClassUtils
						.getQualifiedName(MockUrlFactory.class),
				"com.github.nosan.embedded.cassandra.artifact.proxy.host=localhost",
				"com.github.nosan.embedded.cassandra.artifact.proxy.port=80",
				"com.github.nosan.embedded.cassandra.artifact.proxy.type=SOCKS",
				"com.github.nosan.embedded.cassandra.artifact.read-timeout=4s",
				"com.github.nosan.embedded.cassandra.artifact.connect-timeout=5s");

		assertThat(this.context.getBeansOfType(LocalCassandraFactory.class)).hasSize(1);
		LocalCassandraFactory factory = this.context.getBean(LocalCassandraFactory.class);

		assertThat(factory.getVersion()).isEqualTo(Version.parse("3.11.3"));
		assertThat(factory.isRegisterShutdownHook()).isTrue();
		assertThat(factory.isDaemon()).isFalse();
		assertThat(factory.getStartupTimeout()).isEqualTo(Duration.ofSeconds(30));
		assertThat(factory.isDeleteWorkingDirectory()).isTrue();
		assertThat(factory.isAllowRoot()).isTrue();
		assertThat(factory.getJmxLocalPort()).isEqualTo(7199);
		assertThat(factory.getPort()).isEqualTo(9042);
		assertThat(factory.getRpcPort()).isEqualTo(9160);
		assertThat(factory.getStoragePort()).isEqualTo(7000);
		assertThat(factory.getSslStoragePort()).isEqualTo(7001);
		assertThat(factory.getJvmOptions()).containsExactly("-Dtest.property=property");
		assertThat(factory.getEnvironmentVariables()).containsEntry("NAME", "cassandra");
		assertThat(factory.getWorkingDirectory()).isEqualTo(Paths.get("target/embedded-cassandra"));
		assertThat(factory.getArtifactDirectory()).isEqualTo(Paths.get("target/embedded-cassandra-artifact"));
		assertThat(factory.getJavaHome()).isEqualTo(Paths.get("target/java"));
		assertThat(factory.getLoggingFile()).isNotNull().isEqualTo(getClass().getResource("/logging.xml"));
		assertThat(factory.getTopologyFile()).isNotNull().isEqualTo(getClass().getResource("/topology.properties"));
		assertThat(factory.getRackFile()).isNotNull().isEqualTo(getClass().getResource("/rack.properties"));
		assertThat(factory.getConfigurationFile()).isNotNull().isEqualTo(getClass().getResource("/cassandra.yaml"));
		assertThat(factory.getArtifactFactory()).isInstanceOf(RemoteArtifactFactory.class);
		RemoteArtifactFactory af = (RemoteArtifactFactory) factory.getArtifactFactory();
		assertThat(af).isNotNull();
		assertThat(af.getProxy()).isEqualTo(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 80)));
		assertThat(af.getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
		assertThat(af.getReadTimeout()).isEqualTo(Duration.ofSeconds(4));
		assertThat(af.getUrlFactory()).isInstanceOf(MockUrlFactory.class);
	}

	@Test
	void clusterUseEmbeddedPortAndAddress() {
		load(ClusterConfiguration.class);
		assertThat(this.context.getBeansOfType(Cluster.class)).hasSize(1);
		Cluster cluster = this.context.getBean(Cluster.class);
		try (Session session = cluster.connect()) {
			session.execute("SELECT now() FROM system.local;");
		}
	}

	@Test
	void cqlSessionUseEmbeddedPortAndAddress() {
		load(CqlSessionConfiguration.class);
		assertThat(this.context.getBeansOfType(CqlSession.class)).hasSize(1);
		CqlSession session = this.context.getBean(CqlSession.class);
		session.execute("SELECT now() FROM system.local;");
	}

	@Test
	void clusterFactoryBeanEmbeddedPortAndAddress() {
		load(ClusterFactoryConfiguration.class);
		assertThat(this.context.getBeansOfType(Cluster.class)).hasSize(1);
		Cluster cluster = this.context.getBean(Cluster.class);
		try (Session session = cluster.connect()) {
			session.execute("SELECT now() FROM system.local;");
		}
	}

	@Test
	void customCassandraBean() {
		load(CustomCassandraConfiguration.class);
		assertThat(this.context.getBeansOfType(Cassandra.class)).hasSize(1);
		assertThat(this.context.getBeansOfType(Cluster.class)).hasSize(1);
		Cluster cluster = this.context.getBean(Cluster.class);
		try (Session session = cluster.connect()) {
			session.execute("SELECT now() FROM system.local;");
		}
	}

	@Test
	void cassandraSslBean() {
		load(CassandraSslConfiguration.class,
				"com.github.nosan.embedded.cassandra.configuration-file=classpath:cassandra-ssl.yaml");
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
		ctx.registerShutdownHook();
		ctx.register(EmbeddedCassandraAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class);
		ctx.refresh();
		this.context = ctx;
	}

	@Configuration
	static class CassandraSslConfiguration {

		private final Path truststoreFile;

		private final Path keystoreFile;

		CassandraSslConfiguration(@Value("classpath:cert/truststore.node0") Path truststoreFile,
				@Value("classpath:cert/keystore.node0") Path keystoreFile) {
			this.truststoreFile = truststoreFile;
			this.keystoreFile = keystoreFile;
		}

		@Bean
		public WorkingDirectoryCustomizer sslWorkingDirectoryCustomizer() {
			return (workingDirectory, version) -> {
				Path configuration = workingDirectory.resolve("conf");
				Files.copy(this.keystoreFile, configuration.resolve("keystore.node0"));
				Files.copy(this.truststoreFile, configuration.resolve("truststore.node0"));
			};
		}

		@Bean(destroyMethod = "close")
		public Cluster cluster(Cassandra cassandra) throws Exception {
			Settings settings = cassandra.getSettings();
			SSLContext sslContext = SSLContext.getInstance("SSL");
			TrustManagerFactory tmf = getTrustManagerFactory(this.truststoreFile);
			KeyManagerFactory kmf = getKeyManagerFactory(this.keystoreFile);
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
			return Cluster.builder().addContactPoint(settings.getAddress().getHostAddress()).withoutMetrics()
					.withoutJMXReporting().withPort(settings.getPortOrSslPort())
					.withSSL(RemoteEndpointAwareJdkSSLOptions.builder()
							.withSSLContext(sslContext).build()).build();
		}

		private static KeyManagerFactory getKeyManagerFactory(Path keystoreFile) throws Exception {
			char[] password = "cassandra".toCharArray();
			try (InputStream stream = Files.newInputStream(keystoreFile)) {
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(stream, password);
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(ks, password);
				return kmf;
			}
		}

		private static TrustManagerFactory getTrustManagerFactory(Path truststoreFile) throws Exception {
			char[] password = "cassandra".toCharArray();
			try (InputStream stream = Files.newInputStream(truststoreFile)) {
				KeyStore ts = KeyStore.getInstance("JKS");
				ts.load(stream, password);
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(ts);
				return tmf;
			}
		}

	}

	@Configuration
	static class ClusterConfiguration {

		@Bean(destroyMethod = "close")
		public Cluster cluster(Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			return Cluster.builder().addContactPoint(settings.getAddress().getHostAddress()).withoutMetrics()
					.withoutJMXReporting().withPort(settings.getPortOrSslPort())
					.build();
		}

	}

	@Configuration
	static class CqlSessionConfiguration {

		@Bean(destroyMethod = "close")
		public CqlSession cluster(Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			return CqlSession.builder()
					.withLocalDatacenter("datacenter1")
					.addContactPoint(new InetSocketAddress(settings.getAddress(), settings.getPortOrSslPort()))
					.build();
		}

	}

	@Configuration
	static class ClusterFactoryConfiguration {

		@Bean
		public CassandraClusterFactoryBean cluster(Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			CassandraClusterFactoryBean factoryBean = new CassandraClusterFactoryBean();
			factoryBean.setMetricsEnabled(false);
			factoryBean.setJmxReportingEnabled(false);
			factoryBean.setPort(settings.getPortOrSslPort());
			factoryBean.setContactPoints(settings.getAddress().getHostAddress());
			return factoryBean;
		}

	}

	@Configuration
	static class CustomCassandraConfiguration {

		@Bean(initMethod = "start", destroyMethod = "stop")
		public Cassandra customCassandra() {
			return new LocalCassandraFactory().create();
		}

		@Bean(destroyMethod = "close")
		public Cluster cluster(Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			return Cluster.builder().addContactPoints(settings.getAddress()).withoutMetrics()
					.withoutJMXReporting()
					.withPort(settings.getPortOrSslPort()).build();
		}

	}

	static class ExcludeCassandraPostProcessor implements BeanDefinitionRegistryPostProcessor {

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

	static final class MockUrlFactory implements UrlFactory {

		@Override
		public URL[] create(Version version) {
			return new URL[0];
		}

	}

}
