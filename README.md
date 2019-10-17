# Embedded Cassandra [Spring Boot Starter] 
[![Build Status OSX/Linux](https://img.shields.io/travis/nosan/embedded-cassandra-spring-boot/master.svg?logo=travis&logoColor=white&style=flat)](https://travis-ci.org/nosan/embedded-cassandra-spring-boot) [![Build Status Windows](https://img.shields.io/appveyor/ci/nosan/embedded-cassandra-spring-boot/master.svg?logo=appveyor&logoColor=white&style=flat)](https://ci.appveyor.com/project/nosan/embedded-cassandra-spring-boot)

This project offers `EmbeddedCassandraAutoConfiguration` for [Embedded Cassandra](https://github.com/nosan/embedded-cassandra). 

You can declare your own `CassandraFactory` bean to take control of the `Cassandra` 
instance configuration.


Project is based on:

| embedded-cassandra-spring-boot-starter   |      embedded-cassandra      |  spring-boot-starter |
|----------|:-------------:|------:|
| 2.0.0 |  3.0.0 | 2.2.0.RELEASE |
| 1.1.2 |  2.0.4 | 2.1.9.RELEASE |
| 1.1.1 |  2.0.4 | 2.1.8.RELEASE |
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
        <version>2.0.0</version>
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
$ ./mvnw clean verify
```

#### License

This project uses [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
