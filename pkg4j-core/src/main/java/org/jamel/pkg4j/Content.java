package org.jamel.pkg4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sergey Polovko
 */
public class Content {

    private final static int DEFAULT_MODE = 33188; // = (octal) 0100644

    private final Set<Entry> files;


    private Content(Set<Entry> files) {
        this.files = files;
    }

    public static Builder create() {
        return new Builder();
    }

    public Set<Entry> getFiles() {
        return files;
    }

    /**
     * @author Sergey Polovko
     */
    public static class Entry {

        private final String path;
        private final String prefix;
        private final String owner;
        private final int mode;


        public Entry(String path, String prefix, String owner, int mode) {
            this.path = path;
            this.prefix = prefix;
            this.owner = owner;
            this.mode = mode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return path.equals(((Entry) o).path);
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        public String getPath() {
            return path;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getOwner() {
            return owner;
        }

        public int getMode() {
            return mode;
        }
    }


    /**
     * @author Sergey Polovko
     */
    public static class Builder {
        private final Set<Entry> files = new HashSet<Entry>();

        public Builder pack(String path, String prefix, String owner, int mode) {
            files.add(new Entry(path, prefix, owner, mode));
            return this;
        }

        public Builder pack(String path, String prefix, int mode) {
            return pack(path, prefix, "", mode);
        }

        public Builder pack(String path, String prefix, String owner) {
            return pack(path, prefix, owner, DEFAULT_MODE);
        }

        public Builder pack(String path, String prefix) {
            return pack(path, prefix, "", DEFAULT_MODE);
        }

        public Content build() {
            return new Content(Collections.unmodifiableSet(new HashSet<Entry>(files)));
        }
    }
}
