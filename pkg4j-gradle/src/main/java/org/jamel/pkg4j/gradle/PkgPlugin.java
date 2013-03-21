package org.jamel.pkg4j.gradle;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jamel.pkg4j.gradle.convention.CommonPkgConvention;
import org.jamel.pkg4j.gradle.convention.PkgConvention;
import org.jamel.pkg4j.gradle.tasks.BuildDebTask;
import org.jamel.pkg4j.gradle.tasks.BuildRpmTask;

/**
 * @author Sergey Polovko
 */
public class PkgPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Map<String, Object> plugins = project.getConvention().getPlugins();
        plugins.put("commonPkg", new CommonPkgConvention(project));
        plugins.put("pkg", new PkgConvention(project));

        addTask(project, "buildDeb", BuildDebTask.class, "Build Debian (.deb) package of the project");
        addTask(project, "buildRpm", BuildRpmTask.class, "Build RedHat (.rpm) package of the project");
    }

    private void addTask(Project project, String name, Class<? extends DefaultTask> type, String description) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("type", type);
        properties.put("group", "Build");
        properties.put("description", description);
        project.task(properties, name);
    }
}
