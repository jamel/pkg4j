package org.jamel.pkg4j.deb;

import java.io.File;

import org.jamel.pkg4j.Package;

/**
 * @author Sergey Polovko
 */
public class DebBuilder {

    private final String workingDir;


    public DebBuilder(String workingDir) {
        this.workingDir = workingDir;
    }

    public File build(Package packageInfo) {
        return null;
    }
}
