package org.jamel.pkg4j.rpm;

import java.io.File;

import org.jamel.pkg4j.Package;

/**
 * @author Sergey Polovko
 */
public class RpmBuilder {

    private final String workingDir;


    public RpmBuilder(String workingDir) {
        this.workingDir = workingDir;
    }

    public File build(Package packageInfo) {
        return null;
    }
}
