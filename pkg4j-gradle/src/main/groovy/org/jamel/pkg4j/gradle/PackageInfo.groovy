package org.jamel.pkg4j.gradle

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.gradle.api.Project

/**
 * @author Sergey Polovko
 */
class PackageInfo {

    private final Project project

    def String name
    def String author
    def String description
    def String descriptionTitle
    def String section
    def String changes
    def String secureRing
    def String key

    def Map<String, Set<String>> namedDepends = [:]
    def Set<String> includeDepends = [] as LinkedHashSet<String>
    def Set<String> depends = [] as LinkedHashSet<String>

    def List<Map<String, String>> dirs = []
    def List<Map<String, String>> dirsToPack = []

    def List<String> conffiles = []
    def List<String> postinstCommands = []
    def List<String> prermCommands = []
    def List<String> postrmCommands = []


    PackageInfo(Project project) {
        this.project = project
    }

    def boolean isNeedBuild() {
        StringUtils.isNotEmpty(name)
    }

    private void assertConfigurationComplete() {
        assert name : "Package name should be specified in a pkg configuration block"
        assert author : "Author should be specified in a pkg configuration block"
        assert description : "Package description should be specified in a pkg configuration block"
        assert descriptionTitle : "Package descriptionTitle should be specified in a pkg configuration block"
        def changesFile = project.file(changes)
        assert changesFile.exists() : "Changes log file should exists. File path: ${changesFile.path}"
        assert !dirsToPack.isEmpty() : "Dirs to package should be specified in a pkg configuration block with 'pack' keyword"
        assert project.version : "The project version should be specified"
    }

    private static String firstNotEmpty(String... args) {
        for (String arg : args) {
            if (StringUtils.isNotEmpty(arg)) return arg
        }
        return null
    }

    def void mergeWithCommon(PackageInfo commonPkg) {
        author = firstNotEmpty(author, commonPkg?.author)
        description = firstNotEmpty(description, commonPkg?.description)
        section = firstNotEmpty(section, commonPkg?.section, "unknown")
        changes = firstNotEmpty(changes, commonPkg?.changes, "src/pkg/changes.txt")
        secureRing = firstNotEmpty(secureRing, commonPkg?.secureRing, "~/.gnupg/secring.gpg")
        key = firstNotEmpty(key, commonPkg?.key)

        if (commonPkg != null) {
            dirs.addAll(commonPkg.dirs)
            dirsToPack.addAll(commonPkg.dirsToPack)

            conffiles.addAll commonPkg.conffiles
            postinstCommands.addAll commonPkg.postinstCommands
            prermCommands.addAll(commonPkg.prermCommands)
            postrmCommands.addAll(commonPkg.postrmCommands)
        }

        project.logger.info("depends = ${depends}, commonPkg=${commonPkg}")
        for (String includeDepend : includeDepends) {
            def dependencies = commonPkg?.namedDepends[includeDepend]
            if (dependencies == null) {
                throw new IllegalStateException("Can't include '" + includeDepend + "' from commonPkg configuration")
            }
            depends.addAll(dependencies)
        }
    }

    def Map toContext() {
        assertConfigurationComplete()

        [
            name: name,
            version: project.version,
            author: author,
            description: description,
            descriptionTitle: descriptionTitle,
            section: section,
            time: DateFormatUtils.SMTP_DATETIME_FORMAT.format(new Date()),
            depends: StringUtils.join(depends, ", "),
            dirs: dirs,
            conffiles: conffiles,
            postinstCommands: postinstCommands,
            prermCommands: prermCommands,
            postrmCommands: postrmCommands
        ]
    }

    def void depends(Closure closure) {
        new Object() {
            void on(String dependency) {
                depends << dependency
            }
        }.with closure
    }

    def void depends(String name, Closure closure) {
        def depends = [] as LinkedHashSet<String>
        new Object() {
            void on(String dependency) {
                depends << dependency
            }
        }.with closure
        namedDepends[name] = depends
    }

    def void depends(Map options, Closure closure) {
        includeDepends << options["include"]
        depends closure
    }

    def void dirs(Closure closure) {
        new Object() {
            void create(String dir) {
                dirs << [name: dir]
            }

            void create(Map dirOptions, String name) {
                dirOptions["name"] = name
                dirs << dirOptions
            }

            void pack(Map dirOptions) {
                assert dirOptions["dir"] : "You should specify 'from' parameter in copy section"
                assert dirOptions["prefix"] : "You should specify 'to' parameter in copy section"
                dirsToPack << dirOptions
            }
        }.with closure
    }

    def void sign(Closure closure) {
        with closure
    }

    def void conffiles(Closure closure) {
        new Object(){
            void add(String conffile) {
                conffiles << conffile
            }
        }.with closure
    }

    def void postinst(Closure closure) {
        new Object() {
            void exec(String command) {
                postinstCommands << command
            }
        }.with closure
    }

    def void prerm(Closure closure) {
        new Object() {
            void exec(String command) {
                prermCommands << command
            }
        }.with closure
    }

    def void postrm(Closure closure) {
        new Object() {
            void exec(String command) {
                postrmCommands << command
            }
        }.with closure
    }

    @Override
    def String toString() {
        return "PackageInfo{\n" +
                "  project=" + project +
                ",\n  name='" + name + '\'' +
                ",\n  author='" + author + '\'' +
                ",\n  descriptionTitle='" + descriptionTitle + '\'' +
                ",\n  description='" + description + '\'' +
                ",\n  section='" + section + '\'' +
                ",\n  changes='" + changes + '\'' +
                ",\n  secureRing='" + secureRing + '\'' +
                ",\n  key='" + key + '\'' +
                ",\n  namedDepends=" + namedDepends +
                ",\n  includeDepends=" + includeDepends +
                ",\n  depends=" + depends +
                ",\n  dirs=" + dirs +
                ",\n  dirsToPack=" + dirsToPack +
                ",\n  conffiles=" + conffiles +
                ",\n  postinstCommands=" + postinstCommands +
                ",\n  prermCommands=" + prermCommands +
                ",\n  postrmCommands=" + postrmCommands +
                "\n}";
    }
}
