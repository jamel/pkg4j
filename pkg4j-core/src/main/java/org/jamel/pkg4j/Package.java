package org.jamel.pkg4j;

/**
 * @author Sergey Polovko
 */
public class Package {

    private final String name;
    private final String version;
    private final String author;
    private final String email;
    private final String description;
    private final Depends dependsOn;
    private final Content content;
    private final ChangeLog changeLog;


    private Package(Builder builder) {
        name = builder.name;
        version = builder.version;
        author = builder.author;
        email = builder.email;
        description = builder.description;
        dependsOn = builder.dependsOn;
        content = builder.content;
        changeLog = builder.changeLog;
    }

    public static Builder create() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    public Depends getDependsOn() {
        return dependsOn;
    }

    public Content getContent() {
        return content;
    }

    public ChangeLog getChangeLog() {
        return changeLog;
    }

    /**
     * @author Sergey Polovko
     */
    public static class Builder {

        private String name;
        private String version;
        private String author;
        private String email;
        private String description;
        private Depends dependsOn = Depends.none();
        private Content content;
        private ChangeLog changeLog = ChangeLog.empty();


        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder dependsOn(Depends dependsOn) {
            this.dependsOn = dependsOn;
            return this;
        }

        public Builder content(Content content) {
            this.content = content;
            return this;
        }

        public Builder changeLog(ChangeLog changeLog) {
            this.changeLog = changeLog;
            return this;
        }

        public Package build() {
            return new Package(this);
        }
    }
}
