package org.jamel.pkg4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sergey Polovko
 */
public class Depends {

    private static Depends EMPTY = new Depends(Collections.<String>emptyList());

    private final List<String> dependencies;


    private Depends(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public static Depends none() {
        return EMPTY;
    }

    public static Depends on(String dependency) {
        return new Depends(Arrays.asList(dependency));
    }

    public static Builder many() {
        return new Builder();
    }


    /**
     * @author Sergey Polovko
     */
    public static class Builder {
        private final Set<String> dependencies = new LinkedHashSet<String>();

        public Builder on(String dependency) {
            dependencies.add(dependency);
            return this;
        }

        public Depends build() {
            return new Depends(Collections.unmodifiableList(new ArrayList<String>(dependencies)));
        }
    }
}
