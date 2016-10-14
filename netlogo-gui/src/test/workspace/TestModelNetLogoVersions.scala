// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.nio.file.Paths

import org.scalatest.FunSuite

import org.nlogo.core.{ DummyCompilationEnvironment, DummyExtensionManager, Femto, LiteralParser }
import org.nlogo.api.Version
import org.nlogo.util.SlowTest
import org.nlogo.fileformat, fileformat.NLogoThreeDFormat

class TestModelNetLogoVersions extends FunSuite with SlowTest {
  val paths = ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions")
  val dummyWorkspace = new DummyWorkspace()
  val extensionManager = new ExtensionManager(dummyWorkspace, new JarLoader(dummyWorkspace))
  for(path <- paths)
    test(path, SlowTest.Tag) {
      // if this test suddenly starts failing, make sure all models are the most current version
      val loader =
        fileformat.basicLoader
          .addFormat[Array[String], NLogoThreeDFormat](new NLogoThreeDFormat)

      // 5.x.nlogo is a LS is a test model for checking the graceful handling of version problems.
      if (!path.endsWith("5.x.nlogo")) {
        val model = loader.readModel(Paths.get(path).toUri).get
        assert(Version.compatibleVersion(model.version))
      }
    }
}
