# Embedded Cassandra [Spring Boot Starter] 
![Github CI](https://github.com/nosan/embedded-cassandra-spring-boot-starter/workflows/build/badge.svg)
[![codecov](https://codecov.io/gh/nosan/embedded-cassandra-spring-boot-starter/branch/master/graph/badge.svg?token=SNW1ICHYXL)](https://codecov.io/gh/nosan/embedded-cassandra-spring-boot-starter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra-spring-boot-starter/)

This project includes `AutoConfiguration` for [Embedded Cassandra](https://github.com/nosan/embedded-cassandra).

To configure `CassandraBuilder` before it builds `Cassandra`, the application 
properties can be used. All properties are started with a prefix `cassandra.embedded`.

For example:

```properties
cassandra.embedded.config-file=#Cassandra config file.
cassandra.embedded.config-properties=#Config properties which should be merged with properties from cassandra.yaml.
cassandra.embedded.environment-variables=#Cassandra environment variables.
cassandra.embedded.jvm-options=#Cassandra native Java Virtual Machine (JVM) Options.
cassandra.embedded.logger=#Logger name which consumes Cassandra STDOUT and STDERR outputs.
cassandra.embedded.name=#Cassandra instance name.
cassandra.embedded.register-shutdown-hook=#Sets if the created Cassandra should have a shutdown hook registered.
cassandra.embedded.startup-timeout=#Startup timeout.
cassandra.embedded.system-properties=#Cassandra native Java Virtual Machine (JVM) system parameters.
cassandra.embedded.version=#Cassandra version.
cassandra.embedded.working-directory=#Cassandra working directory.
```

For more advanced builder customizations, you can register an arbitrary number of beans that implements `CassandraBuilderConfigurator`.

You also can register your own `CassandraBuilder` bean to get a full control of `Cassandra` bean instantiation.    

`EmbeddedCassandraAutoConfiguration` can be easily used with `@DataCassandraTest` annotation for testing Cassandra repositories,
just add `@ImportAutoConfiguration(EmbeddedCassandraAutoConfiguration.class)` to your test.
`

#### Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-spring-boot-starter</artifactId>
        <version>4.0.0-SNAPSHOT</version>
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
