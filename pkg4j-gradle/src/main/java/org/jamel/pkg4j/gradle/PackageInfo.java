package org.jamel.pkg4j.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import groovy.lang.Closure;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.gradle.api.Project;

/**
 * @author Sergey Polovko
 */
public class PackageInfo {

    private final Project project;

    private String name;
    private String author;
    private String description;
    private String section;
    private String changes;
    private String secureRing;
    private String key;

    private Map<String, Set<String>> namedDepends = new HashMap<String, Set<String>>();
    private Set<String> includeDepends = new LinkedHashSet<String>();
    private Set<String> depends = new LinkedHashSet<String>();

    private List<Map<String, String>> dirs = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> dirsToPack = new ArrayList<Map<String, String>>();

    private List<String> postinstCommands = new ArrayList<String>();
    private List<String> prermCommands = new ArrayList<String>();


    public PackageInfo(Project project) {
        this.project = project;
    }

    public boolean isNeedBuild() {
        return StringUtils.isNotEmpty(name);
    }

    private void assertConfigurationComplete() {
        assert StringUtils.isNotEmpty(name) : "Package name should be specified in a pkg configuration block";
        assert StringUtils.isNotEmpty(author) : "Author should be specified in a pkg configuration block";
        assert StringUtils.isNotEmpty(description) : "Package description should be specified in a pkg configuration block";
        File changesFile = project.file(changes);
        assert changesFile.exists() : "Changes log file should exists. File path: ${changesFile.path}";
        assert !dirsToPack.isEmpty() : "Dirs to package should be specified in a pkg configuration block with 'pack' keyword";
        assert project.getVersion() != null : "The project version should be specified";
    }

    private static String firstNotEmpty(String... args) {
        for (String arg : args) {
            if (StringUtils.isNotEmpty(arg)) return arg;
        }
        return null;
    }

    public void mergeWithCommon(PackageInfo commonPkg) {
        author = firstNotEmpty(author, commonPkg.author);
        description = firstNotEmpty(description, commonPkg.description);
        section = firstNotEmpty(section, commonPkg.section, "unknown");
        changes = firstNotEmpty(changes, commonPkg.changes, "src/pkg/changes.txt");
        secureRing = firstNotEmpty(secureRing, commonPkg.secureRing, "~/.gnupg/secring.gpg");
        key = firstNotEmpty(key, commonPkg.key);

        dirs.addAll(commonPkg.dirs);
        dirsToPack.addAll(commonPkg.dirsToPack);

        postinstCommands.addAll(commonPkg.postinstCommands);
        prermCommands.addAll(commonPkg.prermCommands);

        for (String includeDepend : includeDepends) {
            depends.addAll(commonPkg.namedDepends.get(includeDepend));
        }
    }

    public Map<String, Object> toContext() {
        assertConfigurationComplete();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("name", name);
        context.put("version", project.getVersion());
        context.put("author", author);
        context.put("description", description);
        context.put("section", section);
        context.put("time", DateFormatUtils.SMTP_DATETIME_FORMAT.format(new Date()));
        context.put("depends", StringUtils.join(depends, ", "));
        context.put("dirs", dirs);
        context.put("postinstCommands", postinstCommands);
        context.put("prermCommands", prermCommands);
        return context;
    }

    public Project getProject() {
        return project;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getSection() {
        return section;
    }

    public String getChanges() {
        return changes;
    }

    public String getSecureRing() {
        return secureRing;
    }

    public String getKey() {
        return key;
    }

    public Map<String, Set<String>> getNamedDepends() {
        return namedDepends;
    }

    public Set<String> getIncludeDepends() {
        return includeDepends;
    }

    public Set<String> getDepends() {
        return depends;
    }

    public List<Map<String, String>> getDirs() {
        return dirs;
    }

    public List<Map<String, String>> getDirsToPack() {
        return dirsToPack;
    }

    public List<String> getPostinstCommands() {
        return postinstCommands;
    }

    public List<String> getPrermCommands() {
        return prermCommands;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PackageInfo");
        sb.append("{project=").append(project);
        sb.append(", name='").append(name).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", section='").append(section).append('\'');
        sb.append(", changes='").append(changes).append('\'');
        sb.append(", secureRing='").append(secureRing).append('\'');
        sb.append(", key='").append(key).append('\'');
        sb.append(", namedDepends=").append(namedDepends);
        sb.append(", includeDepends=").append(includeDepends);
        sb.append(", depends=").append(depends);
        sb.append(", dirs=").append(dirs);
        sb.append(", dirsToPack=").append(dirsToPack);
        sb.append(", postinstCommands=").append(postinstCommands);
        sb.append(", prermCommands=").append(prermCommands);
        sb.append('}');
        return sb.toString();
    }

    protected void depends(Closure closure) {
        closure.call(new Object() {
            protected void on(String dependency) {
                depends.add(dependency);
            }
        });
    }

    protected void depends(String name, Closure closure) {
        final Set<String> deps = new LinkedHashSet<String>();
        closure.call(new Object() {
            protected void on(String dependency) {
                deps.add(dependency);
            }
        });
        namedDepends.put(name, deps);
    }

    protected void depends(Map<String, String> options, Closure closure) {
        includeDepends.add(options.get("include"));
        depends(closure);
    }

    protected void dirs(Closure closure) {
        closure.call(new Object() {
            protected void create(String dir) {
                Map<String, String> dirOptions = new HashMap<String, String>();
                dirOptions.put("name", dir);
                dirs.add(dirOptions);
            }

            protected void create(Map<String, String> dirOptions, String name) {
                dirOptions.put("name", name);
                dirs.add(dirOptions);
            }

            protected void pack(Map<String, String> dirOptions) {
                assert dirOptions.containsKey("dir") : "You should specify 'from' parameter in copy section";
                assert dirOptions.containsKey("prefix") : "You should specify 'to' parameter in copy section";
                dirsToPack.add(dirOptions);
            }
        });
    }

    protected void sign(Closure closure) {
        closure.call(this);
    }

    protected void postinst(Closure closure) {
        closure.call(new Object() {
            protected void exec(String command) {
                postinstCommands.add(command);
            }
        });
    }

    protected void prerm(Closure closure) {
        closure.call(new Object() {
            protected void exec(String command) {
                prermCommands.add(command);
            }
        });
    }
}
