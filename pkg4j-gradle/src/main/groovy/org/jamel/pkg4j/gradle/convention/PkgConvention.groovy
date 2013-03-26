package org.jamel.pkg4j.gradle.convention

import org.gradle.api.Project
import org.jamel.pkg4j.gradle.PackageInfo

/**
 * @author Sergey Polovko
 */
class PkgConvention {

    def List<PackageInfo> packages = []
    def Project project


    PkgConvention(Project project) {
        this.project = project
    }

    def void pkg(Closure closure) {
        PackageInfo packageInfo = new PackageInfo(project)
        packageInfo.with closure
        packages << packageInfo
    }
}
