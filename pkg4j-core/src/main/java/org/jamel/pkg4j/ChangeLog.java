package org.jamel.pkg4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jamel.pkg4j.changes.ChangeLogProvider;
import org.jamel.pkg4j.changes.ChangeSet;
import org.jamel.pkg4j.changes.GitChangeLogProvider;
import org.jamel.pkg4j.changes.MercurialChangeLogProvider;
import org.jamel.pkg4j.changes.TextFileChangeLogProvider;

/**
 * @author Sergey Polovko
 */
public class ChangeLog {

    private final static ChangeLog EMPTY = new ChangeLog(Collections.<ChangeSet>emptyList());

    private final List<ChangeSet> changes;


    private ChangeLog(List<ChangeSet> changes) {
        this.changes = changes;
    }

    public List<ChangeSet> getChanges() {
        return changes;
    }

    public static ChangeLog fromProvider(ChangeLogProvider provider) {
        return new ChangeLog(Collections.unmodifiableList(new ArrayList<ChangeSet>(provider.getChangeLog())));
    }

    public static ChangeLog fromFile(String fileName) {
        return fromProvider(new TextFileChangeLogProvider(fileName));
    }

    public static ChangeLog fromGit() {
        return fromProvider(new GitChangeLogProvider());
    }

    public static ChangeLog fromMercurial() {
        return fromProvider(new MercurialChangeLogProvider());
    }

    public static ChangeLog empty() {
        return EMPTY;
    }
}
