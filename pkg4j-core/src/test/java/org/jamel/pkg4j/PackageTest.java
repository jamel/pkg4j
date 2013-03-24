package org.jamel.pkg4j;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Sergey Polovko
 */
public class PackageTest {

    @Test
    public void createInfo() {
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

        Assert.assertEquals(pkgInfo.getName(), "supper-app");
        Assert.assertEquals(pkgInfo.getVersion(), "0.0.1");
        Assert.assertEquals(pkgInfo.getAuthor(), "Donald Duck");
        Assert.assertEquals(pkgInfo.getEmail(), "the@donald.duck");
        Assert.assertEquals(pkgInfo.getDescription(), "My brand new cool app for ducks");

        Assert.assertTrue(pkgInfo.getDependsOn().getDependencies().size() == 2);
        Assert.assertTrue(pkgInfo.getContent().getFiles().size() == 3);
        Assert.assertTrue(pkgInfo.getChangeLog().getChanges().isEmpty());
    }
}
