// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.nio.file.Paths

import org.scalatest.funsuite.AnyFunSuite

import org.nlogo.api.Version
import org.nlogo.core.{ Femto, LiteralParser }
import org.nlogo.util.SlowTest
import org.nlogo.fileformat.FileFormat

class TestModelNetLogoVersions extends AnyFunSuite with SlowTest {
  val paths = ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions")
  val dummyWorkspace = new DummyWorkspace()
  val extensionManager = new ExtensionManager(dummyWorkspace, new JarLoader(dummyWorkspace))
  for(path <- paths)
    test("model version: " + path, SlowTest.Tag) {
      // if this test suddenly starts failing, make sure all models are the most current version
      val literalParser = Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
      val loader = FileFormat.standardAnyLoader(false, literalParser, false)

      // 5.x.nlogo is a LS is a test model for checking the graceful handling of version problems.
      if (!path.endsWith("5.x.nlogo")) {
        val model = loader.readModel(Paths.get(path).toUri).get
        assert(Version.compatibleVersion(model.version))
      }
    }
}
