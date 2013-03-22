# pkg4j

Java library for create OS specific packages.

## How to build

This project is uses [Gradle](http://gradle.org) as the build tool. For build you need clone source code and simply run one command. All magic will happed automatically.

```shell
git clone git@github.com:jamel/pkg4j.git
cd pkg4j
./gradlew build
```

## Dependencies from your project

### Maven dependency

If you are using [Maven](http://maven.apache.org) as build tool for your project, simply add this dependency to your pom.xml.

```xml
<dependency>
    <groupId>org.jamel.pkg4j</groupId>
    <artifactId>pkg4j-core</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle dependency

For [Gradle](http://gradle.org) projects it is even simpler. Make sure that your dependency block looks like:

```groovy
dependencies {
   compile "org.jamel.pkg4j:pkg4j-core:0.0.1"
   // ... other dependencies
}
```
