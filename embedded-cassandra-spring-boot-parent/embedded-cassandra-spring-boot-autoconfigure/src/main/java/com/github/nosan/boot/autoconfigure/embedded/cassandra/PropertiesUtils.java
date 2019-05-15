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
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;

/**
 * Utility class to add {@code Cassandra} properties into the {@code Application Context}.
 *
 * @author Dmytro Nosan
 * @since 1.0.2
 */
abstract class PropertiesUtils {

	/**
	 * Adds {@code local.cassandra.port},{@code local.cassandra.ssl-port},{@code local.cassandra.rpc-port},
	 * {@code local.cassandra.address} properties into the context.
	 *
	 * @param applicationContext Application Context instance
	 * @param cassandra Apache Cassandra instance
	 */
	static void add(ApplicationContext applicationContext, Cassandra cassandra) {
		Settings settings = cassandra.getSettings();
		Optional<Integer> port = settings.port();
		Optional<Integer> sslPort = settings.sslPort();
		Optional<Integer> rpcPort = settings.rpcPort();
		Optional<InetAddress> address = settings.address();
		addProperties(applicationContext, port.orElseGet(() -> sslPort.orElse(null)),
				sslPort.orElse(null), rpcPort.orElse(null), address.orElse(null));
	}

	private static void addProperties(ApplicationContext context,
			Integer port, Integer sslPort, Integer rpcPort, InetAddress address) {
		if (context instanceof ConfigurableApplicationContext) {
			MutablePropertySources sources = ((ConfigurableApplicationContext) context).getEnvironment()
					.getPropertySources();
			Map<String, Object> properties = getProperties(sources);
			if (port != null) {
				properties.put("local.cassandra.port", port);
			}
			if (sslPort != null) {
				properties.put("local.cassandra.ssl-port", sslPort);
			}
			if (rpcPort != null) {
				properties.put("local.cassandra.rpc-port", rpcPort);
			}
			if (address != null) {
				properties.put("local.cassandra.address", address.getHostAddress());
			}
		}
		if (context.getParent() != null) {
			addProperties(context.getParent(), port, sslPort, rpcPort, address);
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
