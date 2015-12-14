// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version
import org.nlogo.workspace.ModelsLibrary
import org.scalatest.FunSuite
import org.nlogo.util.SlowTest
import org.nlogo.workspace.AbstractWorkspace
import ChecksumsAndPreviews.Previews.needsManualPreview
import org.nlogo.api.CompilerException

class TestCompileAll extends FunSuite with SlowTest {

  // Models whose path contains any of these strings will not be tested at all:
  def excludeModel(path: String) =
    (if (Version.is3D) !path.contains(makePath("3D")) // when in 3D, skip models that aren't in the 3D directory.
      else path.endsWith(".nlogo3d")) || // when not in 3D, skip 3D models
        Seq("Arduino", "QuickTime", "Serial").exists(path.contains)
        // ^^ this branch is a transition away from QTJ, and gogo-serial. Arduino is skipped because it isn't bundled

  // and those are exempt from having their preview commands tested:
  def excludePreviewCommands(path: String) =
    Seq(makePath("extensions"), makePath("models", "test"))
      .exists(path.contains)

  val modelPaths =
    (ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions"))
      .map(new java.io.File(_).getCanonicalPath()).distinct // workaround for https://github.com/NetLogo/NetLogo/issues/765
      .filterNot(excludeModel)

  for (path <- modelPaths) {
    test("compile: " + path, SlowTest.Tag) {
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
      if (!excludePreviewCommands(path))
        compilePreviewCommands(workspace)
      // compile BehaviorSpace experiments
      val lab = HeadlessWorkspace.newLab
      lab.load(HeadlessModelOpener.protocolSection(path))
      lab.names.foreach(lab.newWorker(_).compile(workspace))
    }
    finally {workspace.dispose()}
  }
<<<<<<< 708a3a1018a9f147134cdf7a34cd7349aa80d27b

  def compilePreviewCommands(ws: AbstractWorkspace) {
    if (!(ws.previewCommands.isEmpty || needsManualPreview(ws.previewCommands))) {
      val source = "to __custom-preview-commands " + ws.previewCommands + "\nend"
      try {
        ws.compiler.compileMoreCode(source, None, ws.world.program, ws.getProcedures, ws.getExtensionManager)
      } catch {
        case e: CompilerException => throw new Exception("Error compiling preview commands: " + e.getMessage, e)
      }
    }
  }

  def makePath(folderNames: String*) = {
    val sep = java.io.File.separatorChar.toString
    folderNames.mkString(sep, sep, sep)
  }
}
