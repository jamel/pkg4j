package org.jamel.pkg4j.changes;

import java.util.List;

/**
 * @author Sergey Polovko
 */
public interface ChangeLogProvider {

    List<ChangeSet> getChangeLog();
}
