// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.{ Path, Paths }

import org.nlogo.api.{ PreviewCommands, ThreeDVersion, TwoDVersion, Version }
import org.nlogo.core.CompilerException
import org.nlogo.nvm.Workspace
import org.nlogo.util.{ SlowTest, ThreeDTag, TwoDTag }
import org.nlogo.workspace.{ ExtensionManager, ModelsLibrary }
import org.nlogo.fileformat
import org.scalatest.FunSuite

import scala.util.{ Failure, Success, Try }
import scala.collection.JavaConverters._


class TestCompileAll extends FunSuite with SlowTest {

  // Models whose path contains any of these strings will not be tested at all:
  def excludeModel(path: Path): Boolean = {
    // the vid extension loads javafx on startup, which we don't want in headless mode.
    path.iterator.asScala.map(_.toString).toSet.contains("vid") ||
    // the r extension relies on R, which is system-dependent and we don't want to depend on.
    path.iterator.asScala.map(_.toString).toSet.contains("r") ||
    // This is a LS model specifically for testing version problems.
    path.getFileName.toString.endsWith("5.x.nlogo") ||
    // This is a LS model designed to test widgets with errors.
    path.getFileName.toString.endsWith("LS-Widgets.nlogo")
  }

  // and those are exempt from having their preview commands tested:
  def excludePreviewCommands(path: String) =
    Seq(makePath("extensions"), makePath("models", "test"))
      .exists(path.contains)

  val modelPaths =
    (ModelsLibrary.getModelPaths(TwoDVersion) ++
      ModelsLibrary.getModelPaths(ThreeDVersion) ++
      ModelsLibrary.getModelPathsAtRoot(ExtensionManager.extensionPath, TwoDVersion) ++
      ModelsLibrary.getModelPathsAtRoot(ExtensionManager.extensionPath, ThreeDVersion))
        .map(s => Paths.get(s).toAbsolutePath)
        .distinct
        .filterNot(excludeModel _)

  for (path <- modelPaths) {
    val version = fileformat.modelVersionAtPath(path.toString)
      .getOrElse(throw new RuntimeException("Unable to determine version of " + path.toString))
    val tag = if (version.is3D) ThreeDTag else TwoDTag
    test("compile: " + path.toString, tag, SlowTest.Tag) {
      compile(path.toString, version)
    }
  }

  def compile(path: String, version: Version) {
    val workspace = HeadlessWorkspace.newInstance(version.is3D)
    // this keeps patches from being created, which we don't need,
    // and which was slowing things down - ST 1/13/05
    // and has some other effects - ST 12/6/07
    workspace.compilerTestingMode = true
    try {
      workspace.open(path)
      if (!excludePreviewCommands(path)) compilePreviewCommands(workspace)
      // compile BehaviorSpace experiments
      val lab = HeadlessWorkspace.newLab(version.is3D)
      val protocols = BehaviorSpaceCoordinator.protocolsFromModel(path, version, workspace)
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
