package org.jamel.pkg4j.gradle

import org.apache.commons.lang.NotImplementedException
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.DateFormatUtils
import org.gradle.api.Project
import org.jamel.pkg4j.ChangeLog
import org.jamel.pkg4j.Package.Builder

/**
 * @author Sergey Polovko
 */
class PackageInfo {

    private final Project project
    private final Builder packageBuilder

    def needToBuild

    def Map<String, Set<String>> namedDepends = [:]
    def Set<String> includeDepends = [] as LinkedHashSet<String>
    def Set<String> depends = [] as LinkedHashSet<String>

    def List<Map<String, String>> dirs = []
    def List<Map<String, String>> dirsToPack = []

    def List<String> postinstCommands = []
    def List<String> prermCommands = []


    PackageInfo(Project project) {
        this.project = project
        packageBuilder = org.jamel.pkg4j.Package.create()
    }

    void setName(String name) {
        needToBuild = true
        packageBuilder.name(name)
    }

    void setAuthor(String author) {
        packageBuilder.author(author)
    }

    void setDescription(String description) {
        packageBuilder.description(description)
    }

    void setSection(String section) {
        throw new NotImplementedException()
    }

    void setChanges(String changes) {
        if (changes == "git") {
            packageBuilder.changeLog(ChangeLog.fromGit())
        } else if (changes == "mercurial") {
            packageBuilder.changeLog(ChangeLog.fromMercurial())
        } else if (new File(changes).exists()) {
            packageBuilder.changeLog(ChangeLog.fromFile(changes))
        }
        throw new IllegalArgumentException("Changes must be provided from git, mercurial or plain text file")
    }

    void setSecureRing(String secureRing) {
        throw new NotImplementedException()
    }

    void setKey(String key) {
        throw new NotImplementedException()
    }

    private void assertConfigurationComplete() {
        assert name : "Package name should be specified in a pkg configuration block"
        assert author : "Author should be specified in a pkg configuration block"
        assert description : "Package description should be specified in a pkg configuration block"
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
        author = firstNotEmpty(author, commonPkg.author)
        description = firstNotEmpty(description, commonPkg.description)
        section = firstNotEmpty(section, commonPkg.section, "unknown")
        changes = firstNotEmpty(changes, commonPkg.changes, "src/pkg/changes.txt")
        secureRing = firstNotEmpty(secureRing, commonPkg.secureRing, "~/.gnupg/secring.gpg")
        key = firstNotEmpty(key, commonPkg.key)

        dirs.addAll(commonPkg.dirs)
        dirsToPack.addAll(commonPkg.dirsToPack)

        postinstCommands.addAll commonPkg.postinstCommands
        prermCommands.addAll(commonPkg.prermCommands)

        project.logger.info("depends = ${depends}, commonPkg=${commonPkg}")
        for (String includeDepend : includeDepends) {
            depends.addAll(commonPkg.namedDepends[includeDepend])
        }
    }

    def Map toContext() {
        assertConfigurationComplete()

        [
            name: name,
            version: project.version,
            author: author,
            description: description,
            section: section,
            time: DateFormatUtils.SMTP_DATETIME_FORMAT.format(new Date()),
            depends: StringUtils.join(depends, ", "),
            dirs: dirs,
            postinstCommands: postinstCommands,
            prermCommands: prermCommands
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
}
