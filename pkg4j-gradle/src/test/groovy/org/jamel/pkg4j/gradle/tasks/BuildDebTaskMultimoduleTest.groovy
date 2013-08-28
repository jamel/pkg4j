package org.jamel.pkg4j.gradle.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class BuildDebTaskMultimoduleTest extends BuildDebTaskAbstractTest {

  @Override
  Project getParent() {
    return ProjectBuilder.builder().build()
  }
}
