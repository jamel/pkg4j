package org.jamel.pkg4j.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.jamel.pkg4j.gradle.convention.CommonPkgConvention
import org.jamel.pkg4j.gradle.convention.PkgConvention
import org.junit.Before
import org.junit.Test

class Pkg4jPluginTest {

  def project
  def plugin

  @Before
  public void setUp() {
    project = ProjectBuilder.builder().build()
    plugin = new Pkg4jPlugin()
  }

  @Test
  public void "apply project installs the pkg4j plugin tasks and conventions"() {
    plugin.apply(project)

    assert project.tasks.buildDeb
    assert project.tasks.buildRpm

    assert project.convention.plugins.pkg in PkgConvention
    assert project.convention.plugins.commonPkg in CommonPkgConvention
  }
}
