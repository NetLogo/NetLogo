// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version
import org.nlogo.workspace.ModelsLibrary
import org.scalatest.FunSuite
import org.nlogo.util.SlowTest
import org.nlogo.workspace.AbstractWorkspace
import ChecksumsAndPreviews.Previews.needsManualPreview

class TestCompileAll extends FunSuite with SlowTest{

  // Models whose path contains any of these strings will not be tested:
  val exclusions = Seq("Arduino Example") // see https://github.com/NetLogo/NetLogo/issues/763

  val modelPaths =
    (ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions"))
      .map(new java.io.File(_).getCanonicalPath()).toSet  // workaround for https://github.com/NetLogo/NetLogo/issues/765
      .filterNot(path => exclusions exists path.contains)

  for (path <- modelPaths) {
    test("compile: " + path) {
      compile(path)
    }
  }

  def compile(path: String) {
    val workspace = HeadlessWorkspace.newInstance
    // this keeps patches from being created, which we don't need,
    // and which was slowing things down - ST 1/13/05
    // and has some other effects - ST 12/6/07
    workspace.compilerTestingMode = true
    try {
      workspace.open(path)
      compilePreviewCommands(workspace)
      // compile BehaviorSpace experiments
      val lab = HeadlessWorkspace.newLab
      lab.load(HeadlessModelOpener.protocolSection(path))
      lab.names.foreach(lab.newWorker(_).compile(workspace))
    }
    finally {workspace.dispose()}
  }

  def compilePreviewCommands(ws: AbstractWorkspace) {
    if (!(ws.previewCommands.isEmpty || needsManualPreview(ws.previewCommands))) {
      val source = "to __custom-preview-commands " + ws.previewCommands + "\nend"
      ws.compiler.compileMoreCode(source, None, ws.world.program, ws.getProcedures, ws.getExtensionManager)
    }
  }
}
