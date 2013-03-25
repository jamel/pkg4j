package org.jamel.pkg4j.changes;

import java.util.Collections;
import java.util.List;

/**
 * @author Sergey Polovko
 */
public class TextFileChangeLogProvider implements ChangeLogProvider {

    private final String fileName;


    public TextFileChangeLogProvider(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public List<ChangeSet> getChangeLog() {
        return Collections.emptyList();
    }
}
