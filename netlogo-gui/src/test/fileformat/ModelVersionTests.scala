// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.nio.file.{ Files, Path, Paths }

import org.nlogo.api.Version
import org.nlogo.headless.HeadlessWorkspace
import org.nlogo.util.AnyFunSuiteEx

class ModelVersionTests extends AnyFunSuiteEx {
  test("Model versions match API version") {
    val workspace = HeadlessWorkspace.newInstance
    val loader = FileFormat.standardAnyLoader(false, workspace.compiler.utilities)

    var failures = Seq[String]()

    def checkModel(path: Path, extension: String): Unit = {
      if (path.toString.endsWith(extension)) {
        loader.readModel(path.toUri).foreach { model =>
          if (model.version != Version.version)
            failures = failures :+ s"$path has version ${model.version}"
        }
      }
    }

    Version.set3D(false)

    Files.walk(Paths.get("models")).forEach(checkModel(_, ".nlogox"))

    Version.set3D(true)

    Files.walk(Paths.get("models")).forEach(checkModel(_, ".nlogox3d"))

    Version.set3D(false)

    if (failures.nonEmpty)
      fail(failures.mkString("\n"))
  }
}
