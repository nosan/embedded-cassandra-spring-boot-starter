# Embedded Cassandra [Spring Boot Starter] 
[![Build Status OSX/Linux](https://img.shields.io/travis/nosan/embedded-cassandra-spring-boot/master.svg?logo=travis&logoColor=white&style=flat)](https://travis-ci.org/nosan/embedded-cassandra-spring-boot) [![Build Status Windows](https://img.shields.io/appveyor/ci/nosan/embedded-cassandra-spring-boot/master.svg?logo=appveyor&logoColor=white&style=flat)](https://ci.appveyor.com/project/nosan/embedded-cassandra-spring-boot)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra-spring-boot-starter.svg)](https://search.maven.org/artifact/com.github.nosan/embedded-cassandra-spring-boot-starter)

This project offers `EmbeddedCassandraAutoConfiguration` for [Embedded Cassandra](https://github.com/nosan/embedded-cassandra). 

You can declare your own `CassandraFactory` or/and `ArtifactFactory` bean(s) to take control of the `Cassandra` 
instances configuration.


Project is based on:

| embedded-cassandra-spring-boot-starter   |      embedded-cassandra      |  spring-boot-starter |
|----------|:-------------:|------:|
| 1.1.0 |  2.0.4 | 2.1.7.RELEASE |
| 1.0.3 |  2.0.3 | 2.1.6.RELEASE |
| 1.0.2 |    2.0.2   |   2.1.5.RELEASE |
| 1.0.1 | 2.0.1 |    2.1.5.RELEASE |
| 1.0.0 | 2.0.0 |    2.1.4.RELEASE |
 
#### Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-spring-boot-starter</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>

```

#### Issues

`Embedded Cassandra [Spring Boot]` uses GitHub's issue tracking system to report bugs and feature
requests. If you want to raise an issue, please follow this [link](https://github.com/nosan/embedded-cassandra-spring-boot/issues)
and use predefined `GitHub` templates.

Also see [CONTRIBUTING.md](CONTRIBUTING.md) if you wish to submit pull requests.

#### Build

`Embedded Cassandra [Spring Boot]` can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper). You also need `JDK 1.8`.

```bash
$ ./mvnw clean install
```

#### License

This project uses [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)


#### Properties
    
    com.github.nosan.embedded.cassandra.allow-root=true#Allow running Cassandra under a root user.
    com.github.nosan.embedded.cassandra.artifact-directory=#Directory to extract an artifact.
    com.github.nosan.embedded.cassandra.artifact.connect-timeout=30s#Connection timeout
    com.github.nosan.embedded.cassandra.artifact.proxy.host=#The host of the proxy to use.
    com.github.nosan.embedded.cassandra.artifact.proxy.port=#The port of the proxy to use.
    com.github.nosan.embedded.cassandra.artifact.proxy.type=HTTP#The proxy type to use.
    com.github.nosan.embedded.cassandra.artifact.read-timeout=30s#Read timeout
    com.github.nosan.embedded.cassandra.artifact.url-factory=com.github.nosan.embedded.cassandra.local.artifact.DefaultUrlFactory#Factory class to determine URLs for downloading an archive.
    com.github.nosan.embedded.cassandra.configuration-file=#Cassandra's configuration file.
    com.github.nosan.embedded.cassandra.daemon=true#Marks Cassandra's threads as daemons.
    com.github.nosan.embedded.cassandra.delete-working-directory=#Delete the working directory after the successful stop.
    com.github.nosan.embedded.cassandra.java-home=#Java home directory.
    com.github.nosan.embedded.cassandra.jmx-local-port=#JMX port to listen on.
    com.github.nosan.embedded.cassandra.jvm-options=#JVM options that should be associated with Cassandra.
    com.github.nosan.embedded.cassandra.environment-variables=#Environment variables that should be associated with Cassandra.
    com.github.nosan.embedded.cassandra.logging-file=#Cassandra's logging file.
    com.github.nosan.embedded.cassandra.port=#The native transport port to listen for the clients on.
    com.github.nosan.embedded.cassandra.rack-file=#Configuration file to determine which data centers and racks nodes belong to.
    com.github.nosan.embedded.cassandra.register-shutdown-hook=false#Register a shutdown hook with the JVM runtime.
    com.github.nosan.embedded.cassandra.rpc-port=#Thrift port for client connections.
    com.github.nosan.embedded.cassandra.ssl-storage-port=#The ssl port for inter-node communication.
    com.github.nosan.embedded.cassandra.startup-timeout=1m#Startup timeout.
    com.github.nosan.embedded.cassandra.storage-port=#The port for inter-node communication.
    com.github.nosan.embedded.cassandra.topology-file=#Configuration file for data centers and rack names and to determine network topology so that requests are routed efficiently and allows the database to distribute replicas evenly.
    com.github.nosan.embedded.cassandra.version=3.11.4#Cassandra's Version.
    com.github.nosan.embedded.cassandra.working-directory=#Cassandra's working directory.

