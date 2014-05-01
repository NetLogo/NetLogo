// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import org.nlogo.api.Version
import org.nlogo.workspace.ModelsLibrary
import org.scalatest.FunSuite
import org.nlogo.util.SlowTest
import org.nlogo.api.model.ModelReader

object TestCompileAll {
  // core branch doesn't have these features - ST 1/11/12
  def badPath(path: String): Boolean = {
    import java.io.File.separatorChar
    def pathMatches(bad: String) =
      path.toUpperCase.containsSlice(separatorChar + bad + separatorChar)
    pathMatches("SYSTEM DYNAMICS") ||
      pathMatches("GIS") ||
      pathMatches("QUICKTIME EXTENSION") ||
      pathMatches("HUBNET ACTIVITIES") ||
      pathMatches("CURRICULAR MODELS") ||
      pathMatches("SOUND") ||
      path.containsSlice("Frogger") || // uses sound extension
      path.containsSlice("Sound Machines") || // uses sound extension
      path.containsSlice("GoGoMonitor") ||
      path.containsSlice("Movie Example") ||
      path.endsWith(".nlogo3d")
  }
}

class TestCompileAll extends FunSuite with SlowTest {

  for (path <- ModelsLibrary.getModelPaths.filterNot(TestCompileAll.badPath))
    test("compile: " + path) {
      compile(path)
    }

  for (path <- ModelsLibrary.getModelPaths.filterNot(TestCompileAll.badPath))
    test("readWriteRead: " + path) {
      readWriteRead(path)
    }

  def readWriteRead(path: String) {
    val workspace = HeadlessWorkspace.newInstance
    try {
      val modelContents = org.nlogo.api.FileIO.file2String(path)
      val model = ModelReader.parseModel(modelContents, workspace)
      val newModel = ModelReader.parseModel(ModelReader.formatModel(model, workspace), workspace)
      assert(model.code == newModel.code)
      assert(model.widgets == newModel.widgets)
      assert(model.info == newModel.info)
      assert(model.version == newModel.version)
      assert(model.turtleShapes == newModel.turtleShapes)
      assert(model.behaviorSpace == newModel.behaviorSpace)
      assert(model.linkShapes == newModel.linkShapes)
      assert(model.previewCommands == newModel.previewCommands)
      assert(model == newModel)
    }
    finally workspace.dispose()
  }

  def compile(path: String) {
    val workspace = HeadlessWorkspace.newInstance
    // compilerTestingMode keeps patches from being created, which we don't need (and which was
    // slowing things down), and has some other effects too - ST 1/13/05, 12/6/07
    workspace.compilerTestingMode = true
    try {
      workspace.open(path)
      val lab = HeadlessWorkspace.newLab
      lab.load(HeadlessModelOpener.protocolSection(path))
      lab.names.foreach(lab.newWorker(_).compile(workspace))
    }
    finally workspace.dispose()
  }

}
