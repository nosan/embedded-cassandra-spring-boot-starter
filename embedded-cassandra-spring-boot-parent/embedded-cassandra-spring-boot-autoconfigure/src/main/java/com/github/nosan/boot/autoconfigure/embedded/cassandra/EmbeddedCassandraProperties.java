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

import java.net.Proxy.Type;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import com.github.nosan.embedded.cassandra.local.artifact.DefaultUrlFactory;
import com.github.nosan.embedded.cassandra.local.artifact.UrlFactory;

/**
 * Configuration properties for Embedded Cassandra.
 *
 * @author Dmytro Nosan
 * @since 0.0.1
 */
@ConfigurationProperties(prefix = "com.github.nosan.embedded.cassandra")
public class EmbeddedCassandraProperties {

	/**
	 * JVM options that should be associated with Cassandra.
	 */
	private final List<String> jvmOptions = new ArrayList<>();

	/**
	 * Artifact configuration.
	 */
	private final Artifact artifact = new Artifact();

	/**
	 * Cassandra Version.
	 */
	private String version = "3.11.4";

	/**
	 * Cassandra directory.
	 */
	private Path workingDirectory;

	/**
	 * Directory to extract an artifact.
	 */
	private Path artifactDirectory;

	/**
	 * Cassandra configuration file.
	 */
	private Resource configurationFile;

	/**
	 * Cassandra logging file.
	 */
	private Resource loggingFile;

	/**
	 * Configuration file to determine which data centers and racks nodes belong to.
	 */
	private Resource rackFile;

	/**
	 * Configuration file for data centers and rack names and to determine network
	 * topology so that requests are routed efficiently and allows the database to
	 * distribute replicas evenly.
	 */
	private Resource topologyFile;

	/**
	 * Java home directory.
	 */
	private Path javaHome;

	/**
	 * Register a shutdown hook with the JVM runtime.
	 */
	private boolean registerShutdownHook = false;

	/**
	 * Whether to allow running Cassandra under root user.
	 */
	private boolean allowRoot = true;

	/**
	 * The native transport port to listen for the clients on.
	 */
	private Integer port;

	/**
	 * Thrift port for client connections.
	 */
	private Integer rpcPort;

	/**
	 * The port for inter-node communication.
	 */
	private Integer storagePort;

	/**
	 * The ssl port for inter-node communication.
	 */
	private Integer sslStoragePort;

	/**
	 * JMX port to listen on.
	 */
	private Integer jmxLocalPort;

	/**
	 * Delete the working directory after success Cassandra stop.
	 */
	private boolean deleteWorkingDirectory = true;

	/**
	 * Marks Cassandra threads as daemons.
	 */
	private boolean daemon = true;

	/**
	 * Startup timeout.
	 */
	private Duration startupTimeout = Duration.ofMinutes(1);

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getRpcPort() {
		return this.rpcPort;
	}

	public void setRpcPort(Integer rpcPort) {
		this.rpcPort = rpcPort;
	}

	public Integer getStoragePort() {
		return this.storagePort;
	}

	public void setStoragePort(Integer storagePort) {
		this.storagePort = storagePort;
	}

	public Integer getSslStoragePort() {
		return this.sslStoragePort;
	}

	public void setSslStoragePort(Integer sslStoragePort) {
		this.sslStoragePort = sslStoragePort;
	}

	public Integer getJmxLocalPort() {
		return this.jmxLocalPort;
	}

	public void setJmxLocalPort(Integer jmxLocalPort) {
		this.jmxLocalPort = jmxLocalPort;
	}

	public boolean isRegisterShutdownHook() {
		return this.registerShutdownHook;
	}

	public void setRegisterShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
	}

	public boolean isAllowRoot() {
		return this.allowRoot;
	}

	public void setAllowRoot(boolean allowRoot) {
		this.allowRoot = allowRoot;
	}

	public Artifact getArtifact() {
		return this.artifact;
	}

	public List<String> getJvmOptions() {
		return this.jvmOptions;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Path getWorkingDirectory() {
		return this.workingDirectory;
	}

	public void setWorkingDirectory(Path workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public Resource getConfigurationFile() {
		return this.configurationFile;
	}

	public void setConfigurationFile(Resource configurationFile) {
		this.configurationFile = configurationFile;
	}

	public Resource getLoggingFile() {
		return this.loggingFile;
	}

	public void setLoggingFile(Resource loggingFile) {
		this.loggingFile = loggingFile;
	}

	public Resource getRackFile() {
		return this.rackFile;
	}

	public void setRackFile(Resource rackFile) {
		this.rackFile = rackFile;
	}

	public Resource getTopologyFile() {
		return this.topologyFile;
	}

	public void setTopologyFile(Resource topologyFile) {
		this.topologyFile = topologyFile;
	}

	public Path getJavaHome() {
		return this.javaHome;
	}

	public void setJavaHome(Path javaHome) {
		this.javaHome = javaHome;
	}

	public Path getArtifactDirectory() {
		return this.artifactDirectory;
	}

	public void setArtifactDirectory(Path artifactDirectory) {
		this.artifactDirectory = artifactDirectory;
	}

	public boolean isDeleteWorkingDirectory() {
		return this.deleteWorkingDirectory;
	}

	public void setDeleteWorkingDirectory(boolean deleteWorkingDirectory) {
		this.deleteWorkingDirectory = deleteWorkingDirectory;
	}

	public boolean isDaemon() {
		return this.daemon;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public Duration getStartupTimeout() {
		return this.startupTimeout;
	}

	public void setStartupTimeout(Duration startupTimeout) {
		this.startupTimeout = startupTimeout;
	}

	/**
	 * Artifact properties.
	 */
	public static class Artifact {

		/**
		 * Factory class to determine URLs for downloading an archive.
		 */
		private Class<? extends UrlFactory> urlFactory = DefaultUrlFactory.class;

		/**
		 * Proxy configuration.
		 */
		private Proxy proxy;

		/**
		 * Read timeout specifies the timeout when reading from InputStream when a
		 * connection is established to a resource.
		 */
		private Duration readTimeout = Duration.ofSeconds(30);

		/**
		 * Connection timeout to be used when opening a communications link to the
		 * resource referenced by URLConnection.
		 */
		private Duration connectTimeout = Duration.ofSeconds(30);

		public Class<? extends UrlFactory> getUrlFactory() {
			return this.urlFactory;
		}

		public void setUrlFactory(Class<? extends UrlFactory> urlFactory) {
			this.urlFactory = urlFactory;
		}

		public Proxy getProxy() {
			return this.proxy;
		}

		public void setProxy(Proxy proxy) {
			this.proxy = proxy;
		}

		public Duration getReadTimeout() {
			return this.readTimeout;
		}

		public void setReadTimeout(Duration readTimeout) {
			this.readTimeout = readTimeout;
		}

		public Duration getConnectTimeout() {
			return this.connectTimeout;
		}

		public void setConnectTimeout(Duration connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

	}

	/**
	 * Proxy properties.
	 */
	public static class Proxy {

		/**
		 * The host of the proxy to use.
		 */
		private String host;

		/**
		 * The port of the proxy to use.
		 */
		private Integer port;

		/**
		 * The proxy type to use.
		 */
		private Type type = Type.HTTP;

		public Type getType() {
			return this.type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public String getHost() {
			return this.host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public Integer getPort() {
			return this.port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

	}

}
