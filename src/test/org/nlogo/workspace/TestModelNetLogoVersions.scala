// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.FunSuite
import org.nlogo.api.{ FileIO, ModelReader, Version }
import org.nlogo.util.SlowTest

class TestModelNetLogoVersions extends FunSuite with SlowTest {
  val paths = ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions")
  for(path <- paths)
    test(path) {
      val version = ModelReader.parseVersion(ModelReader.parseModel(FileIO.file2String(path)))
      assert(Version.compatibleVersion(version))
    }
}
