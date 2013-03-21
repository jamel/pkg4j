package org.jamel.pkg4j.gradle.convention;

import java.util.ArrayList;
import java.util.List;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.jamel.pkg4j.gradle.PackageInfo;

/**
 * @author Sergey Polovko
 */
public class PkgConvention {

    private final Project project;
    private final List<PackageInfo> packages = new ArrayList<PackageInfo>();
    private PackageInfo lastPackage;


    public PkgConvention(Project project) {
        this.project = project;
    }

    public void pkg(Closure closure) {
        lastPackage = new PackageInfo(project);
        packages.add(lastPackage);
        closure.call(lastPackage);
    }

    public List<PackageInfo> getPackages() {
        return packages;
    }
}
