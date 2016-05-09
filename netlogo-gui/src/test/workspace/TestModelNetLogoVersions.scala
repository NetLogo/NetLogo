// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.nio.file.Paths

import org.scalatest.FunSuite

import org.nlogo.core.{ Femto, LiteralParser }
import org.nlogo.api.Version
import org.nlogo.util.SlowTest
import org.nlogo.fileformat

class TestModelNetLogoVersions extends FunSuite with SlowTest {
  private val litParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  val paths = ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions")
  for(path <- paths)
    test(path, SlowTest.Tag) {
      val model = fileformat.standardLoader(litParser, _ => identity).readModel(Paths.get(path).toUri).get
      assert(Version.compatibleVersion(model.version))
    }
}
