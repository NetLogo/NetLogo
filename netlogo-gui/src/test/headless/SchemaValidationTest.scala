// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.Paths

import org.nlogo.api.{ ThreeDVersion, TwoDVersion }
import org.nlogo.workspace.{ ExtensionManager, ModelsLibrary }
import org.nlogo.fileformat.SchemaValidator

import org.scalatest.FunSuite

class SchemaValidationTest extends FunSuite with SchemaValidator {
  val modelPaths =
    (ModelsLibrary.getModelPaths(TwoDVersion) ++
      ModelsLibrary.getModelPaths(ThreeDVersion) ++
      ModelsLibrary.getModelPathsAtRoot(ExtensionManager.extensionPath, TwoDVersion) ++
      ModelsLibrary.getModelPathsAtRoot(ExtensionManager.extensionPath, ThreeDVersion))
        .map(s => Paths.get(s).toAbsolutePath)
        .distinct

  for (path <- modelPaths) {
    test(s"${path.toString} validates against netlogox xml schema") { new Helper {
      validate(path)
    } }
  }
}
