// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import org.nlogo.core.{ Femto, LiteralParser }
import org.nlogo.core.model.ModelReader
import org.nlogo.api.{ FileIO, Version }
import org.nlogo.fileformat
import org.nlogo.workspace.ModelsLibrary
import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.util.SlowTest

import
  scala.util.matching.Regex

object TestCompileAll {
  val HeadlessSupportedExtensions =
    Seq("ARRAY", "MATRIX", "PROFILER", "SAMPLE", "SAMPLE-SCALA", "TABLE")

  val UnsupportedPrimitives =
    Seq("HUBNET-RESET")

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
      path.containsSlice("HubNet") ||
      path.containsSlice("Frogger") || // uses sound extension
      path.containsSlice("Sound Machines") || // uses sound extension
      path.containsSlice("GoGoMonitor") ||
      path.containsSlice("Movie Example") ||
      path.containsSlice("Anisogamy.nlogo") || // uses behaviorspace-experiment-name
      path.endsWith("5.x.nlogo") || // explicitly 5.x models
      path.endsWith(".nlogo3d")
  }

  def goodModel(text: String): Option[String] = {
    val extensionsRegex = new Regex("""EXTENSIONS \[([^]]*)\]""", "exts")
    val cleanedText = text.toUpperCase.replaceAll("\n", "")
    val onlySupportedPrimitives =
      ! UnsupportedPrimitives.exists(cleanedText.contains)
    val onlyValidExtensions =
      extensionsRegex.findFirstMatchIn(cleanedText).map {
        extensionMatch =>
          val extensions = extensionMatch.group(1).trim.split(" ").filterNot(_ == "")
          extensions.forall(HeadlessSupportedExtensions.contains)
      } getOrElse true
    if (onlyValidExtensions && onlySupportedPrimitives)
      Some(text)
    else
      None
  }
}

class TestCompileAll extends AnyFunSuite  {
  for {
    path <- ModelsLibrary.getModelPaths.filterNot(TestCompileAll.badPath)
    text <- TestCompileAll.goodModel(FileIO.fileToString(path))
  }  {
      test("compile: " + path, SlowTest.Tag) {
        compile(path, text)
      }
      test("readWriteRead: " + path, SlowTest.Tag) {
        readWriteRead(path, text)
      }
    }

  for(path <- (ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions")).filterNot(TestCompileAll.badPath))
    test("version: " + path, SlowTest.Tag) {
      val workspace = HeadlessWorkspace.newInstance
      val version = ModelReader.parseModel(FileIO.fileToString(path), workspace.parser, Map()).version
      assert(Version.compatibleVersion(version))
    }

  private val literalParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  def readWriteRead(path: String, text: String) {
    val workspace = HeadlessWorkspace.newInstance
    try {
      val loader = fileformat.standardAnyLoader(literalParser)
      val model = loader.readModel(text, "nlogo").get
      val newModel = loader.readModel(loader.sourceString(model, "nlogo").get, "nlogo").get
      assertResult(model.code)(newModel.code)
      assertResult(model.widgets)(newModel.widgets)
      assertResult(model.info)(newModel.info)
      assertResult(model.version)(newModel.version)
      assertResult(model.turtleShapes)(newModel.turtleShapes)
      assertResult(model.linkShapes)(newModel.linkShapes)
      assertResult(model.optionalSections)(model.optionalSections)
    }
    finally workspace.dispose()
  }

  def compile(path: String, text: String) {
    val workspace = HeadlessWorkspace.newInstance
    // compilerTestingMode keeps patches from being created, which we don't need (and which was
    // slowing things down), and has some other effects too - ST 1/13/05, 12/6/07
    workspace.compilerTestingMode = true
    try {
      workspace.open(path, true)
      val lab = HeadlessWorkspace.newLab
      val protocols = BehaviorSpaceCoordinator.protocolsFromModel(path)
      protocols.foreach(lab.newWorker(_).compile(workspace))
    }
    finally workspace.dispose()
  }

}
