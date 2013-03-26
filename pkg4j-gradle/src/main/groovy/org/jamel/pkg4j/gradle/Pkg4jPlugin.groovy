package org.jamel.pkg4j.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jamel.pkg4j.gradle.convention.CommonPkgConvention
import org.jamel.pkg4j.gradle.convention.PkgConvention
import org.jamel.pkg4j.gradle.tasks.BuildDebTask
import org.jamel.pkg4j.gradle.tasks.BuildRpmTask

/**
 * @author Sergey Polovko
 */
class Pkg4jPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.convention.plugins.commonPkg = new CommonPkgConvention(project)
        project.convention.plugins.pkg = new PkgConvention(project)

        project.task("buildDeb",
                group: "Build",
                type: BuildDebTask,
                description: "Build Debian (.deb) package of the project") {}

        project.task("buildRpm",
                group: "Build",
                type: BuildRpmTask,
                description: "Build RedHat (.rpm) package of the project") {}
    }
}
