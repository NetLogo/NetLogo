// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import scala.util.{ Failure, Success, Try }

import org.nlogo.api.{ ExtensionManager, PreviewCommands, Version }
import org.nlogo.core.CompilerException
import org.nlogo.nvm.Workspace
import org.nlogo.util.SlowTest
import org.nlogo.workspace.ModelsLibrary
import org.scalatest.FunSuite

class TestCompileAll extends FunSuite with SlowTest {

  // Models whose path contains any of these strings will not be tested at all:
  def excludeModel(path: String): Boolean = {
    val sep = java.io.File.separator
    if (Version.is3D) ! path.contains(makePath("3D")) // when in 3D, skip models that aren't in the 3D directory.
    else (path.endsWith(".nlogo3d") || // when not in 3D, skip 3D models
      path.contains(s"${sep}vid${sep}") || // the vid extension loads javafx on startup, which we don't want in headless mode.
      path.contains(s"${sep}r${sep}") || // the r extension relies on R, which is system-dependent and we don't want to depend on.
      path.contains(s"${sep}python${sep}") || // the python extension relies on Python, which is system-dependent and we don't want to depend on.
      path.endsWith("5.x.nlogo") || // This is a LS model specifically for testing version problems.
      path.endsWith("LS-Widgets.nlogo")) // This is a LS model designed to test widgets with errors.
  }

  // and those are exempt from having their preview commands tested:
  def excludePreviewCommands(path: String) =
    Seq(makePath("extensions"), makePath("models", "test"))
      .exists(path.contains)

  val modelPaths =
    (ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot(ExtensionManager.extensionsPath.toString))
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
      if (!excludePreviewCommands(path)) compilePreviewCommands(workspace)
      // compile BehaviorSpace experiments
      val lab = HeadlessWorkspace.newLab
      val protocols = BehaviorSpaceCoordinator.protocolsFromModel(path, workspace)
      protocols.foreach(lab.newWorker(_).compile(workspace))
    } finally workspace.dispose()
  }

  def compilePreviewCommands(ws: Workspace): Unit =
    if (ws.previewCommands.isInstanceOf[PreviewCommands.Compilable]) {
      Try (ws.compileCommands(ws.previewCommands.source)) match {
        case Failure(e: CompilerException) =>
          fail("Error compiling preview commands: " + e.getMessage)
        case Failure(e) => throw e
        case Success(_) => // commands compiled correctly
      }
    }

  def makePath(folderNames: String*) = {
    val sep = java.io.File.separatorChar.toString
    folderNames.mkString(sep, sep, sep)
  }

}
