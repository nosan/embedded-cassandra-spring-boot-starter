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

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;

/**
 * {@link FactoryBean} to register {@link Cassandra} bean.
 * <p>
 * After the successful start of {@link Cassandra}, the following properties may be added to the {@link Environment}.
 * <ul>
 * <ol>local.cassandra.port</ol>
 * <ol>local.cassandra.ssl-port</ol>
 * <ol>local.cassandra.address</ol>
 * </ul>
 *
 * @author Dmytro Nosan
 * @since 0.0.1
 */
class EmbeddedCassandraFactoryBean implements FactoryBean<Cassandra>, InitializingBean, DisposableBean {

	private final Cassandra cassandra;

	private final ApplicationContext applicationContext;

	EmbeddedCassandraFactoryBean(Cassandra cassandra, ApplicationContext applicationContext) {
		Assert.notNull(cassandra, () -> "Cassandra must not be null");
		Assert.notNull(applicationContext, () -> "Application Context must not be null");
		this.cassandra = cassandra;
		this.applicationContext = applicationContext;
	}

	@Override
	public void destroy() {
		this.cassandra.stop();
	}

	@Override
	public void afterPropertiesSet() {
		this.cassandra.start();
		setProperties(this.applicationContext, this.cassandra.getSettings());
	}

	@Override
	public Cassandra getObject() {
		return this.cassandra;
	}

	@Override
	public Class<?> getObjectType() {
		return Cassandra.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	private static void setProperties(ApplicationContext context, Settings settings) {
		if (context instanceof ConfigurableApplicationContext) {
			MutablePropertySources sources = ((ConfigurableApplicationContext) context).getEnvironment()
					.getPropertySources();
			Map<String, Object> properties = getProperties(sources);
			settings.getPort().ifPresent(port -> properties.put("local.cassandra.port", port));
			settings.getSslPort().ifPresent(sslPort -> properties.put("local.cassandra.ssl-port", sslPort));
			settings.getAddress().map(InetAddress::getHostAddress)
					.ifPresent(host -> properties.put("local.cassandra.address", host));
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

}
