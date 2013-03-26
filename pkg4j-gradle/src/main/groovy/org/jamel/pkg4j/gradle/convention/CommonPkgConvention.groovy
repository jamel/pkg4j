package org.jamel.pkg4j.gradle.convention

import org.gradle.api.Project
import org.jamel.pkg4j.gradle.PackageInfo

/**
 * @author Sergey Polovko
 */
class CommonPkgConvention {

    def final PackageInfo packageInfo


    CommonPkgConvention(Project project) {
        this.packageInfo = new PackageInfo(project)
    }

    def void commonPkg(Closure closure) {
        packageInfo.with closure
    }
}
