# Gradle packaging plugin

This plugin wil help you to build OS packages (.deb or .rpm) from Java (or Groovy, or Scala) sources.

# Quick Start
For start using this plugin just include this lines in your [Gradle](http://gradle.org) build script.

```groovy
// Pull the plugin from Maven Central
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies { 
        classpath "org.jamel.pkg4j:pkg4j-gradle:0.0.2" 
    }
}

// Invoke the plugin
apply plugin: 'pkg4j'

version = "1.0.0"

// Provide information from build package

pkg {
    name = "my-supper-app"
    description = "This is my Supper App."
    changes = "src/pkg/changes.txt"

    dirs {
        create "/var/cache/supper-app", owner: "app-user"
        pack dir: "src/pkg/etc", prefix: "/etc", mode: "775"
        pack dir: "src/pkg/bin", prefix: "/usr/lib/supper-app/bin"
        pack dir: "build/libs",  prefix: "/usr/lib/supper-app/libs"
    }

    depends {
        on "openjdk-6-jre | default-jre"
        on "postfix (>= 2.7.0)"
    }

    postinst { exec "update-rc.d supper-app defaults 90 20" }
    prerm    { exec "update-rc.d -f supper-app remove" }
}

```

In changes.txt you must provide release information. Something like this:

```
release date=15:00 03.14.2013,version=1.0.0
 * first stable release
release date=14:00 03.01.2013,version=0.0.1
 * initial release
```

Then you can use buildDeb task:

```
    ./gradlew build buidlDeb
```
