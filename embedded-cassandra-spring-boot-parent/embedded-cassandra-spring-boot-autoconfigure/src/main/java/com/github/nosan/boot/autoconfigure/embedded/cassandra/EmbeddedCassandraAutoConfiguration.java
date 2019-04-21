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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.stream.Collectors;

import com.datastax.driver.core.Cluster;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.WorkingDirectoryCustomizer;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.UrlFactory;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Embedded Cassandra.
 *
 * @author Dmytro Nosan
 * @since 0.0.1
 */
@Configuration
@EnableConfigurationProperties(EmbeddedCassandraProperties.class)
@AutoConfigureBefore(CassandraAutoConfiguration.class)
@ConditionalOnClass({Cassandra.class, ArchiveEntry.class, Logger.class})
public class EmbeddedCassandraAutoConfiguration {

	private static final String EMBEDDED_CASSANDRA = "embeddedCassandra";

	private final EmbeddedCassandraProperties properties;

	private final ApplicationContext applicationContext;

	public EmbeddedCassandraAutoConfiguration(EmbeddedCassandraProperties properties,
			ApplicationContext applicationContext) {
		this.properties = properties;
		this.applicationContext = applicationContext;
	}

	@Bean(EMBEDDED_CASSANDRA)
	@ConditionalOnMissingBean(Cassandra.class)
	public EmbeddedCassandraFactoryBean embeddedCassandra(CassandraFactory embeddedCassandraFactory) {
		return new EmbeddedCassandraFactoryBean(embeddedCassandraFactory.create(), this.applicationContext);
	}

	@Bean
	@ConditionalOnMissingBean
	public CassandraFactory embeddedCassandraFactory(ArtifactFactory embeddedCassandraArtifactFactory,
			ObjectProvider<WorkingDirectoryCustomizer> workingDirectoryCustomizers) throws IOException {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		EmbeddedCassandraProperties properties = this.properties;
		factory.setJmxLocalPort(properties.getJmxLocalPort());
		factory.setRpcPort(properties.getRpcPort());
		factory.setPort(properties.getPort());
		factory.setSslStoragePort(properties.getSslStoragePort());
		factory.setStoragePort(properties.getStoragePort());
		factory.setVersion(StringUtils.hasText(properties.getVersion())
				? Version.parse(properties.getVersion()) : null);
		factory.setArtifactFactory(embeddedCassandraArtifactFactory);
		factory.setWorkingDirectory(properties.getWorkingDirectory());
		factory.setArtifactDirectory(properties.getArtifactDirectory());
		factory.setConfigurationFile(getURL(properties.getConfigurationFile()));
		factory.setJavaHome(properties.getJavaHome());
		factory.setLoggingFile(getURL(properties.getLoggingFile()));
		factory.setTopologyFile(getURL(properties.getTopologyFile()));
		factory.setRackFile(getURL(properties.getRackFile()));
		factory.setJvmOptions(properties.getJvmOptions());
		factory.setRegisterShutdownHook(properties.isRegisterShutdownHook());
		factory.setAllowRoot(properties.isAllowRoot());
		factory.setDeleteWorkingDirectory(properties.isDeleteWorkingDirectory());
		factory.setWorkingDirectoryCustomizers(workingDirectoryCustomizers.orderedStream()
				.collect(Collectors.toList()));
		return factory;
	}

	@Bean
	@ConditionalOnMissingBean
	public ArtifactFactory embeddedCassandraArtifactFactory() {
		RemoteArtifactFactory factory = new RemoteArtifactFactory();
		EmbeddedCassandraProperties.Artifact artifact = this.properties.getArtifact();
		factory.setConnectTimeout(artifact.getConnectTimeout());
		factory.setReadTimeout(artifact.getReadTimeout());
		factory.setDirectory(artifact.getDirectory());
		Class<? extends UrlFactory> urlFactory = artifact.getUrlFactory();
		if (urlFactory != null) {
			factory.setUrlFactory(BeanUtils.instantiateClass(urlFactory));
		}
		EmbeddedCassandraProperties.Proxy proxy = artifact.getProxy();
		if (proxy != null && Proxy.Type.DIRECT != proxy.getType()) {
			factory.setProxy(new java.net.Proxy(proxy.getType(),
					new InetSocketAddress(proxy.getHost(), proxy.getPort())));
		}
		return factory;
	}

	private static URL getURL(Resource resource) throws IOException {
		return (resource != null) ? resource.getURL() : null;
	}

	/**
	 * Additional configuration to ensure that {@link Cluster} and {@link CassandraClusterFactoryBean} beans depends on
	 * the {@link #EMBEDDED_CASSANDRA} bean.
	 */
	@Configuration
	@ConditionalOnClass(Cluster.class)
	protected static class EmbeddedCassandraClusterDependencyConfiguration
			extends AbstractDependsOnBeanFactoryPostProcessor {

		private static final boolean factoryBeanClassPresent =
				ClassUtils.isPresent("org.springframework.data.cassandra.config.CassandraClusterFactoryBean",
						EmbeddedCassandraClusterDependencyConfiguration.class.getClassLoader());

		public EmbeddedCassandraClusterDependencyConfiguration() {
			super(Cluster.class, getFactoryClass(), EMBEDDED_CASSANDRA);
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
			if (beanFactory.containsBean(EMBEDDED_CASSANDRA)) {
				super.postProcessBeanFactory(beanFactory);
			}
		}

		private static Class<? extends FactoryBean<?>> getFactoryClass() {
			if (factoryBeanClassPresent) {
				return CassandraClusterFactoryBean.class;
			}
			return null;
		}

	}

}
