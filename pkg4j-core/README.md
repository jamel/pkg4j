Core library for building OS specific packages.

# Quick start

For build .deb or .rpm packages you have to write simple code like this:

```java
// (1) create package information
Package pkgInfo = Package.create()
        .name("supper-app")
        .version("0.0.1")
        .author("Donald Duck")
        .email("the@donald.duck")
        .description("My brand new cool app for ducks")
        .dependsOn(Depends.many()
                .on("duck-daemon (>= 0.12.1)")
                .on("default-jre")
                .build())
        .content(Content.create()
                .pack("src/bin", "/usr/local/bin", 0744)
                .pack("src/etc", "/etc/supper-app")
                .pack("target/report.txt", "/var/cache/supper-app", "app-user", 0644)
                .build())
        .changeLog(ChangeLog.fromGit())
        .build();


// (2) build .deb package and GPG signed .changes file
DebBuilder debBuilder = new DebBuilder("target/debs");
File deb = debBuilder.build(pkgInfo);
File changes = Signer.sign(debBuilder.buildChanges(pkgInfo));


// (3) build .rpm package
RpmBuilder rpmBuilder = new RpmBuilder("target/rpms");
File rpm = rpmBuilder.build(pkgInfo);
```

And after run this you will get your packages in target folder.

