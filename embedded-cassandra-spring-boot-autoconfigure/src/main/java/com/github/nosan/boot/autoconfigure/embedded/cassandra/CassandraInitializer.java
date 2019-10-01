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
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;
import com.github.nosan.embedded.cassandra.commons.io.Resource;
import com.github.nosan.embedded.cassandra.commons.io.SpringResource;

/**
 * Initializes a {@link Cassandra} with the given CQL scripts.
 *
 * @author Dmytro Nosan
 */
class CassandraInitializer implements InitializingBean {

	private final EmbeddedCassandraProperties properties;

	private final ApplicationContext applicationContext;

	private final CassandraConnection connection;

	CassandraInitializer(EmbeddedCassandraProperties properties, ApplicationContext applicationContext,
			CassandraConnection connection) {
		this.properties = properties;
		this.applicationContext = applicationContext;
		this.connection = connection;
	}

	@Override
	public void afterPropertiesSet() throws IOException {
		EmbeddedCassandraProperties properties = this.properties;
		Charset encoding = properties.getScriptsEncoding();
		Resource[] resources = getResources(properties.getScripts(), this.applicationContext);
		if (resources.length > 0) {
			CqlDataSet dataSet = CqlDataSet.ofResources(encoding, resources);
			dataSet.forEach(this.connection::execute);
		}
	}

	private static Resource[] getResources(List<String> scripts, ApplicationContext applicationContext)
			throws IOException {
		List<Resource> resources = new ArrayList<>();
		for (String script : scripts) {
			for (org.springframework.core.io.Resource resource : doGetResources(applicationContext, script)) {
				if (resource.exists()) {
					resources.add(new SpringResource(resource));
				}
			}
		}
		return resources.toArray(new Resource[0]);
	}

	private static org.springframework.core.io.Resource[] doGetResources(ApplicationContext context,
			String location) throws IOException {
		org.springframework.core.io.Resource[] resources = context.getResources(location);
		Arrays.sort(resources, Comparator.comparing(resource -> toURL(resource).toString()));
		return resources;
	}

	private static URL toURL(org.springframework.core.io.Resource resource) {
		try {
			return resource.getURL();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
