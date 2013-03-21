package org.jamel.pkg4j.gradle.convention;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.jamel.pkg4j.gradle.PackageInfo;

/**
 * @author Sergey Polovko
 */
public class CommonPkgConvention {

    private final PackageInfo packageInfo;


    public CommonPkgConvention(Project project) {
        this.packageInfo = new PackageInfo(project);
    }

    void commonPkg(Closure closure) {
        closure.call(packageInfo);
    }

    public PackageInfo getPackage() {
        return packageInfo;
    }
}
