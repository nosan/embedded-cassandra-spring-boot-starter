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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import com.github.nosan.embedded.cassandra.api.Cassandra;

/**
 * Configuration properties for an embedded {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
@ConfigurationProperties(prefix = "com.github.nosan.embedded.cassandra")
public class EmbeddedCassandraProperties {

	/**
	 * CQL script resource references.
	 */
	private final List<String> cqlScripts = new ArrayList<>();

	/**
	 * Environment variables that should be passed to Cassandra's process.
	 */
	private final Map<String, Object> environmentVariables = new LinkedHashMap<>();

	/**
	 * JVM options that should be passed to Cassandra's process.
	 */
	private final List<String> jvmOptions = new ArrayList<>();

	/**
	 * System properties that should be passed to Cassandra's process.
	 */
	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	/**
	 * These properties replace any properties in the cassandra.yaml.
	 */
	private final Map<String, Object> configProperties = new LinkedHashMap<>();

	/**
	 * CQL scripts encoding.
	 */
	private Charset cqlScriptsEncoding = StandardCharsets.UTF_8;

	/**
	 * Whether the root user is able to start Cassandra or not.
	 */
	private boolean rootAllowed = true;

	/**
	 * Whether the thread which reads Cassandra's output should be a daemon or not.
	 */
	private boolean daemon = false;

	/**
	 * Whether shutdown hook should be registered or not.
	 */
	private boolean registerShutdownHook = false;

	/**
	 * Cassandra's logger.
	 */
	private String logger = "embeddedCassandra";

	/**
	 * Cassandra's startup timeout.
	 */
	private Duration timeout = Duration.ofSeconds(90);

	/**
	 * Cassandra's name.
	 */
	@Nullable
	private String name;

	/**
	 * Cassandra's version.
	 */
	@Nullable
	private String version = "3.11.6";

	/**
	 * Cassandra's  config.
	 */
	@Nullable
	private Resource config;

	/**
	 * Cassandra's rack config.
	 */
	@Nullable
	private Resource rackConfig;

	/**
	 * Cassandra's topology config.
	 */
	@Nullable
	private Resource topologyConfig;

	/**
	 * Cassandra's working directory.
	 */
	@Nullable
	private Path workingDirectory;

	/**
	 * Cassandra's java home.
	 */
	@Nullable
	private Path javaHome;

	/**
	 * Sets the address.
	 */
	@Nullable
	private InetAddress address;

	/**
	 * Cassandra's native transport port.
	 */
	@Nullable
	private Integer port = 0;

	/**
	 * Cassandra's native transport ssl port.
	 */
	@Nullable
	private Integer sslPort;

	/**
	 * Cassandra's rpc port.
	 */
	@Nullable
	private Integer rpcPort = 0;

	/**
	 * Cassandra's storage port.
	 */
	@Nullable
	private Integer storagePort = 0;

	/**
	 * Cassandra's storage ssl port.
	 */
	@Nullable
	private Integer sslStoragePort;

	/**
	 * Cassandra's jmx local port.
	 */
	@Nullable
	private Integer jmxLocalPort = 0;

	public List<String> getCqlScripts() {
		return this.cqlScripts;
	}

	public Map<String, Object> getEnvironmentVariables() {
		return this.environmentVariables;
	}

	public List<String> getJvmOptions() {
		return this.jvmOptions;
	}

	public Map<String, Object> getSystemProperties() {
		return this.systemProperties;
	}

	public Map<String, Object> getConfigProperties() {
		return this.configProperties;
	}

	public Charset getCqlScriptsEncoding() {
		return this.cqlScriptsEncoding;
	}

	public void setCqlScriptsEncoding(Charset cqlScriptsEncoding) {
		this.cqlScriptsEncoding = cqlScriptsEncoding;
	}

	public boolean isRootAllowed() {
		return this.rootAllowed;
	}

	public void setRootAllowed(boolean rootAllowed) {
		this.rootAllowed = rootAllowed;
	}

	public boolean isDaemon() {
		return this.daemon;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public boolean isRegisterShutdownHook() {
		return this.registerShutdownHook;
	}

	public void setRegisterShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
	}

	public String getLogger() {
		return this.logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	public Duration getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	@Nullable
	public String getName() {
		return this.name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	@Nullable
	public String getVersion() {
		return this.version;
	}

	public void setVersion(@Nullable String version) {
		this.version = version;
	}

	@Nullable
	public Resource getConfig() {
		return this.config;
	}

	public void setConfig(@Nullable Resource config) {
		this.config = config;
	}

	@Nullable
	public Resource getRackConfig() {
		return this.rackConfig;
	}

	public void setRackConfig(@Nullable Resource rackConfig) {
		this.rackConfig = rackConfig;
	}

	@Nullable
	public Resource getTopologyConfig() {
		return this.topologyConfig;
	}

	public void setTopologyConfig(@Nullable Resource topologyConfig) {
		this.topologyConfig = topologyConfig;
	}

	@Nullable
	public Path getWorkingDirectory() {
		return this.workingDirectory;
	}

	public void setWorkingDirectory(@Nullable Path workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	@Nullable
	public Path getJavaHome() {
		return this.javaHome;
	}

	public void setJavaHome(@Nullable Path javaHome) {
		this.javaHome = javaHome;
	}

	@Nullable
	public InetAddress getAddress() {
		return this.address;
	}

	public void setAddress(@Nullable InetAddress address) {
		this.address = address;
	}

	@Nullable
	public Integer getPort() {
		return this.port;
	}

	public void setPort(@Nullable Integer port) {
		this.port = port;
	}

	@Nullable
	public Integer getSslPort() {
		return this.sslPort;
	}

	public void setSslPort(@Nullable Integer sslPort) {
		this.sslPort = sslPort;
	}

	@Nullable
	public Integer getRpcPort() {
		return this.rpcPort;
	}

	public void setRpcPort(@Nullable Integer rpcPort) {
		this.rpcPort = rpcPort;
	}

	@Nullable
	public Integer getStoragePort() {
		return this.storagePort;
	}

	public void setStoragePort(@Nullable Integer storagePort) {
		this.storagePort = storagePort;
	}

	@Nullable
	public Integer getSslStoragePort() {
		return this.sslStoragePort;
	}

	public void setSslStoragePort(@Nullable Integer sslStoragePort) {
		this.sslStoragePort = sslStoragePort;
	}

	@Nullable
	public Integer getJmxLocalPort() {
		return this.jmxLocalPort;
	}

	public void setJmxLocalPort(@Nullable Integer jmxLocalPort) {
		this.jmxLocalPort = jmxLocalPort;
	}

}
