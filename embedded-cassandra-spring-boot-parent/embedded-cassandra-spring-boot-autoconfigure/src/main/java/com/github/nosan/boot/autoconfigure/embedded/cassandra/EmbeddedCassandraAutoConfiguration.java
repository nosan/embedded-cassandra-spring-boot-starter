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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.datastax.driver.core.Cluster;
import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Settings;
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

	@Bean(destroyMethod = "stop")
	@ConditionalOnMissingBean(Cassandra.class)
	public Cassandra embeddedCassandra(CassandraFactory embeddedCassandraFactory,
			ApplicationContext applicationContext) {
		Cassandra cassandra = embeddedCassandraFactory.create();
		cassandra.start();
		setProperties(applicationContext, cassandra.getSettings());
		return cassandra;
	}

	@Bean
	@ConditionalOnMissingBean
	public CassandraFactory embeddedCassandraFactory(EmbeddedCassandraProperties properties,
			ArtifactFactory embeddedCassandraArtifactFactory,
			ObjectProvider<WorkingDirectoryCustomizer> workingDirectoryCustomizers) throws IOException {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		factory.setJmxLocalPort(properties.getJmxLocalPort());
		factory.setRpcPort(properties.getRpcPort());
		factory.setPort(properties.getPort());
		factory.setSslStoragePort(properties.getSslStoragePort());
		factory.setStoragePort(properties.getStoragePort());
		factory.setVersion(properties.getVersion());
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
		factory.setArtifactFactory(embeddedCassandraArtifactFactory);
		return factory;
	}

	@Bean
	@ConditionalOnMissingBean
	public ArtifactFactory embeddedCassandraArtifactFactory(EmbeddedCassandraProperties properties) {
		RemoteArtifactFactory factory = new RemoteArtifactFactory();
		EmbeddedCassandraProperties.Artifact artifact = properties.getArtifact();
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

	private static void setProperties(ApplicationContext context, Settings settings) {
		if (context instanceof ConfigurableApplicationContext) {
			MutablePropertySources sources = ((ConfigurableApplicationContext) context).getEnvironment()
					.getPropertySources();
			Map<String, Object> properties = getProperties(sources);
			settings.port().ifPresent(port -> properties.put("local.cassandra.port", port));
			settings.sslPort().ifPresent(port -> properties.put("local.cassandra.ssl-port", port));
			settings.rpcPort().ifPresent(port -> properties.put("local.cassandra.rpc-port", port));
			settings.address().ifPresent(host -> properties.put("local.cassandra.address", host.getHostAddress()));
		}
		if (context.getParent() != null) {
			setProperties(context.getParent(), settings);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> getProperties(MutablePropertySources sources) {
		PropertySource<?> propertySource = sources.get("local.cassandra");
		if (propertySource == null) {
			propertySource = new MapPropertySource("local.cassandra", new LinkedHashMap<>());
			sources.addFirst(propertySource);
		}
		return (Map<String, Object>) propertySource.getSource();
	}

	/**
	 * Additional configuration to ensure that {@link Cluster} bean depends on {@link Cassandra} bean.
	 */
	@Configuration
	@ConditionalOnClass(Cluster.class)
	static class EmbeddedCassandraClusterDependencyConfiguration
			extends AbstractCassandraDependsOnBeanFactoryPostProcessor {

		EmbeddedCassandraClusterDependencyConfiguration() {
			super(Cluster.class, getFactoryBeanClass());
		}

		private static Class<? extends FactoryBean<?>> getFactoryBeanClass() {
			ClassLoader cl = EmbeddedCassandraClusterDependencyConfiguration.class.getClassLoader();
			if (ClassUtils.isPresent("org.springframework.data.cassandra.config.CassandraClusterFactoryBean", cl)) {
				return CassandraClusterFactoryBean.class;
			}
			return null;
		}

	}

	/**
	 * Additional configuration to ensure that {@link CqlSession} bean depends on {@link Cassandra} bean.
	 */
	@Configuration
	@ConditionalOnClass(CqlSession.class)
	static class EmbeddedCassandraCqlSessionDependencyConfiguration
			extends AbstractCassandraDependsOnBeanFactoryPostProcessor {

		EmbeddedCassandraCqlSessionDependencyConfiguration() {
			super(CqlSession.class, null);
		}

	}

	private abstract static class AbstractCassandraDependsOnBeanFactoryPostProcessor
			extends AbstractDependsOnBeanFactoryPostProcessor {

		AbstractCassandraDependsOnBeanFactoryPostProcessor(Class<?> beanClass,
				Class<? extends FactoryBean<?>> factoryBeanClass) {
			super(beanClass, factoryBeanClass, Cassandra.class);
		}

	}

	private abstract static class AbstractDependsOnBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

		private final Class<?> beanClass;

		private final Class<? extends FactoryBean<?>> factoryBeanClass;

		private final Class<?>[] dependsOn;

		AbstractDependsOnBeanFactoryPostProcessor(Class<?> beanClass,
				Class<? extends FactoryBean<?>> factoryBeanClass, Class<?>... dependsOn) {
			this.beanClass = beanClass;
			this.factoryBeanClass = factoryBeanClass;
			this.dependsOn = dependsOn;
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			for (String beanName : getBeanNames(this.beanClass, this.factoryBeanClass, beanFactory)) {
				BeanDefinition definition = getDefinition(beanName, beanFactory);
				String[] dependencies = definition.getDependsOn();
				for (Class<?> type : this.dependsOn) {
					for (String bean : getBeanNames(type, beanFactory)) {
						dependencies = StringUtils.addStringToArray(dependencies, bean);
					}
				}
				definition.setDependsOn(dependencies);
			}
		}

		private static Set<String> getBeanNames(Class<?> beanClass, Class<?> factoryBeanClass,
				ListableBeanFactory beanFactory) {
			Set<String> names = new LinkedHashSet<>(Arrays.asList(getBeanNames(beanClass, beanFactory)));
			if (factoryBeanClass != null) {
				for (String name : getBeanNames(factoryBeanClass, beanFactory)) {
					names.add(BeanFactoryUtils.transformedBeanName(name));
				}
			}
			return names;
		}

		private static String[] getBeanNames(Class<?> beanClass, ListableBeanFactory beanFactory) {
			return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, beanClass, true, false);
		}

		private static BeanDefinition getDefinition(String beanName, ConfigurableListableBeanFactory beanFactory) {
			try {
				return beanFactory.getBeanDefinition(beanName);
			}
			catch (NoSuchBeanDefinitionException ex) {
				BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
				if (parentBeanFactory instanceof ConfigurableListableBeanFactory) {
					return getDefinition(beanName, (ConfigurableListableBeanFactory) parentBeanFactory);
				}
				throw ex;
			}
		}

	}

}
