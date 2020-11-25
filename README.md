# Embedded Cassandra [Spring Boot Starter] 
![Github CI](https://github.com/nosan/embedded-cassandra-spring-boot-starter/workflows/build/badge.svg)
[![codecov](https://codecov.io/gh/nosan/embedded-cassandra-spring-boot-starter/branch/master/graph/badge.svg?token=SNW1ICHYXL)](https://codecov.io/gh/nosan/embedded-cassandra-spring-boot-starter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra-spring-boot-starter/)
[![javadoc](https://javadoc.io/badge2/com.github.nosan/embedded-cassandra-spring-boot-autoconfigure/javadoc.svg)](https://javadoc.io/doc/com.github.nosan/embedded-cassandra-spring-boot-autoconfigure)


This project includes `AutoConfiguration` for [Embedded Cassandra](https://github.com/nosan/embedded-cassandra).

To configure `CassandraBuilder` before it builds `Cassandra`, the application 
properties can be used. All properties are started with a prefix `cassandra.embedded`.

For example:

```properties
#Cassandra config file.
cassandra.embedded.config-file=classpath:cassandra.yaml
#Config properties, that should be merged with properties from cassandra.yaml.
cassandra.embedded.config-properties.[native_transport_port]=9042
#Cassandra environment variables.
cassandra.embedded.environment-variables.JAVA_HOME=~/java8
#Cassandra native Java Virtual Machine (JVM) Options.
cassandra.embedded.jvm-options=-Xmx512m
#Logger name, that consumes Cassandra STDOUT and STDERR outputs.
cassandra.embedded.logger=Cassandra
#Cassandra instance name.
cassandra.embedded.name=cassandra-0
#Sets if the created Cassandra should have a shutdown hook registered.
cassandra.embedded.register-shutdown-hook=true
#Startup timeout.
cassandra.embedded.startup-timeout=2m
#Cassandra native Java Virtual Machine (JVM) system parameters.
cassandra.embedded.system-properties.[cassandra.jmx.local.port]=7199
#Cassandra version.
cassandra.embedded.version=3.11.9
#Cassandra working directory.
cassandra.embedded.working-directory=target/cassandra-3.11.9
#Additional resources, that should be copied into the working directory.
cassandra.embedded.working-directory-resources.[conf/cassandra.yaml]=classpath:cassandra.yaml
```

For more advanced builder customizations, you can register an arbitrary number of beans that implements `CassandraBuilderConfigurator`.

You also can register your own `CassandraBuilder` bean to get a full control of `Cassandra` bean instantiation.    

`EmbeddedCassandraAutoConfiguration` can be easily used with `@DataCassandraTest` annotation for testing Cassandra repositories,
just add `@ImportAutoConfiguration(EmbeddedCassandraAutoConfiguration.class)` to your test.

#### Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-spring-boot-starter</artifactId>
        <version>4.0.0</version>
    </dependency>
</dependencies>
```
#### Issues

`Embedded Cassandra [Spring Boot Starter]` uses GitHub's issue tracking system to report bugs and feature
requests. If you want to raise an issue, please follow this [link](https://github.com/nosan/embedded-cassandra-spring-boot-starter/issues)
and use predefined `GitHub` templates.

Also see [CONTRIBUTING.md](CONTRIBUTING.md) if you wish to submit pull requests.


#### Build

`Embedded Cassandra [Spring Boot Starter]` can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper). You also need `JDK 1.8`.

```bash
$ ./mvnw clean verify
```

#### License

This project uses [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
