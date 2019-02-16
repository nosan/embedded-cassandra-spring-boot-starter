# Embedded Cassandra [Spring Boot] 
[![Build Status OSX/Linux](https://img.shields.io/travis/nosan/embedded-cassandra-spring-boot/master.svg?logo=travis&logoColor=white&style=flat)](https://travis-ci.org/nosan/embedded-cassandra-spring-boot) [![Build Status Windows](https://img.shields.io/appveyor/ci/nosan/embedded-cassandra-spring-boot/master.svg?logo=appveyor&logoColor=white&style=flat)](https://ci.appveyor.com/project/nosan/embedded-cassandra-spring-boot)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra-spring-boot-autoconfigure.svg)](https://search.maven.org/search?q=g:com.github.nosan%20AND%20(a:embedded-cassandra-spring-boot-autoconfigure))

This project offers `EmbeddedCassandraAutoConfiguration` for [Embedded Cassandra](https://github.com/nosan/embedded-cassandra). 

You can declare your own `CassandraFactory` or/and `ArtifactFactory` bean(s) to take control of the `Cassandra` 
instances configuration.

If you want to know on which port(s) and address the `Cassandra` is running, get the following properties:
- `local.cassandra.port`
- `local.cassandra.ssl-port`
- `local.cassandra.address`


#### Maven

```xml
<dependencies>
<dependency>
    <groupId>com.github.nosan</groupId>
    <artifactId>embedded-cassandra-spring-boot-autoconfigure</artifactId>
    <version>${version}</version>
</dependency>
<dependency>
    <groupId>com.github.nosan</groupId>
    <artifactId>embedded-cassandra</artifactId>
    <version>${embedded-cassandra.version}</version>
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

This project uses [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)


#### Properties

    com.github.nosan.embedded.cassandra.allow-root=false#Whether to allow running Cassandra as a root or not.
    com.github.nosan.embedded.cassandra.artifact-directory=#Directory to extract an artifact.
    com.github.nosan.embedded.cassandra.artifact.connect-timeout=30s#Connection timeout to be used when opening a communications link to the resource referenced by URLConnection.
    com.github.nosan.embedded.cassandra.artifact.directory=#The directory to save an archive.
    com.github.nosan.embedded.cassandra.artifact.proxy.host=#The host of the proxy to use.
    com.github.nosan.embedded.cassandra.artifact.proxy.port=#The port of the proxy to use.
    com.github.nosan.embedded.cassandra.artifact.proxy.type=HTTP#The proxy type to use.
    com.github.nosan.embedded.cassandra.artifact.read-timeout=30s#Read timeout specifies the timeout when reading from InputStream when a connection is established to a resource.
    com.github.nosan.embedded.cassandra.artifact.url-factory=com.github.nosan.embedded.cassandra.local.artifact.DefaultUrlFactory#Factory class to determine URLs for downloading an archive.
    com.github.nosan.embedded.cassandra.commit-log-archiving-file=#Commit log archiving file.
    com.github.nosan.embedded.cassandra.configuration-file=#Cassandra configuration file.
    com.github.nosan.embedded.cassandra.java-home=#Java home directory.
    com.github.nosan.embedded.cassandra.jmx-port=7199#JMX port to listen on.
    com.github.nosan.embedded.cassandra.jvm-options=#JVM options that should be associated with Cassandra.
    com.github.nosan.embedded.cassandra.logback-file=#Cassandra logback file.
    com.github.nosan.embedded.cassandra.rack-file=#Configuration file to determine which data centers and racks nodes belong to.
    com.github.nosan.embedded.cassandra.register-shutdown-hook=true#Register a shutdown hook with the JVM runtime.
    com.github.nosan.embedded.cassandra.startup-timeout=1m#Startup timeout.
    com.github.nosan.embedded.cassandra.topology-file=#Configuration file for data centers and rack names and to determine network topology so that requests are routed efficiently and allows the database to distribute replicas evenly.
    com.github.nosan.embedded.cassandra.version=3.11.3#Cassandra Version.
    com.github.nosan.embedded.cassandra.working-directory=#Cassandra directory. If directory is not specified, then the temporary directory will be used.

