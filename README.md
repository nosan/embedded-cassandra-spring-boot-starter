# Embedded Cassandra [Spring Boot Starter] 

This project includes `AutoConfiguration` for [Embedded Cassandra](https://github.com/nosan/embedded-cassandra).

To configure CassandraBuilder before it builds Cassandra, the application 
properties can be used. All properties are started with a prefix `cassandra.embedded`.

For example:

```properties
cassandra.embedded.config-properties.native_transport_port=9042
cassandra.embedded.environment-variables.JAVA_HOME="/Users/me/jdk8"
cassandra.embedded.system-properties.cassandra.jmx.local.port=7199
cassandra.embedded.jvm-options=-Xmx512m,-Xms512m
cassandra.embedded.logger=Cassandra
cassandra.embedded.name=cassandra-0
cassandra.embedded.register-shutdown-hook=true
cassandra.embedded.version=3.11.9
cassandra.embedded.startup-timeout=90s
cassandra.embedded.working-directory=target/cassandra-3.11.9
```

For more advanced builder customizations,you can register an arbitrary number of beans that `implements` CassandraBuilderConfigurator.

You also can register your own CassandraBuilder bean to get a full control of Cassandra bean instantiation.    

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

`Embedded Cassandra [Spring Boot]` can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper). You also need `JDK 1.8`.

```bash
$ ./mvnw clean verify
```

#### License

This project uses [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
