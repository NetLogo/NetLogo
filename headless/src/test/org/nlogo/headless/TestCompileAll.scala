// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.CompilerException
import org.nlogo.api.PreviewCommands
import org.nlogo.api.Version
import org.nlogo.workspace.AbstractWorkspaceScala
import org.nlogo.workspace.ModelsLibrary
import org.scalatest.FunSuite
import org.nlogo.util.SlowTest

class TestCompileAll extends FunSuite with SlowTest {

  // Models whose path contains any of these strings will not be tested at all:
  def excludeModel(path: String) =
    Seq(
      "Arduino Example", // see https://github.com/NetLogo/NetLogo/issues/763
      "Oil Cartel HubNet", // see https://github.com/NetLogo/models/issues/51
      "GoGo", // until the gogo extension is updated on 6.x -- NP 2014-04-26
      "QuickTime" // because the qtj extension is not built on Travis
    ).exists(path.contains) ||
      (if (Version.is3D) !path.contains(makePath("3D")) // when in 3D, skip models that aren't in the 3D directory.
      else path.endsWith(".nlogo3d")) // when not in 3D, skip 3D models

  // and those are exempt from having their preview commands tested:
  def excludePreviewCommands(path: String) =
    Seq(makePath("extensions"), makePath("models", "test"))
      .exists(path.contains)

  val modelPaths =
    (ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions"))
      .map(new java.io.File(_).getCanonicalPath()).distinct // workaround for https://github.com/NetLogo/NetLogo/issues/765
      .filterNot(excludeModel)

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
      if (!excludePreviewCommands(path))
        compilePreviewCommands(workspace)
      // compile BehaviorSpace experiments
      val lab = HeadlessWorkspace.newLab
      lab.load(HeadlessModelOpener.protocolSection(path))
      lab.names.foreach(lab.newWorker(_).compile(workspace))
    } finally workspace.dispose()
  }

  def compilePreviewCommands(ws: AbstractWorkspaceScala): Unit =
    ws.previewCommands match {
      case commands: PreviewCommands.Compilable =>
        try {
          ws.compileCommands(commands.source)
        } catch {
          case e: CompilerException => throw new Exception("Error compiling preview commands: " + e.getMessage, e)
        }
      case _ => // non-compilable (i.e., manual) previews don't need testing
    }

  def makePath(folderNames: String*) = {
    val sep = java.io.File.separatorChar.toString
    folderNames.mkString(sep, sep, sep)
  }
}
