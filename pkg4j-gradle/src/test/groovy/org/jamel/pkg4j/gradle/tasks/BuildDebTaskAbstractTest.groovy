package org.jamel.pkg4j.gradle.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jamel.pkg4j.gradle.Pkg4jPlugin
import org.jamel.pkg4j.gradle.convention.CommonPkgConvention
import org.jamel.pkg4j.gradle.convention.PkgConvention
import org.junit.Before
import org.junit.Test

abstract class BuildDebTaskAbstractTest {

  def project
  def plugin

  @Before
  public void setUp() {
    project = ProjectBuilder.builder()
        .withName("BuildDebTestProject")
        .withProjectDir(new File("testdir"))
        .withParent(getParent())
        .build()
    plugin = new Pkg4jPlugin()
  }

  abstract Project getParent();

  @Test
  public void "puts defaults to pkg convention"() {
    plugin.apply(project)

    project.configure(project) {
      pkg {
      }
    }

    assert project.convention.plugins.pkg in PkgConvention
    assert project.convention.plugins.commonPkg in CommonPkgConvention

    assert project.tasks.buildDeb

    project.tasks.buildDeb.run()

    assert project.convention.plugins.pkg.packages[0].section == 'unknown'
    assert project.convention.plugins.pkg.packages[0].changes == "src/pkg/changes.txt"
    assert project.convention.plugins.pkg.packages[0].secureRing == "~/.gnupg/secring.gpg"
  }
}
