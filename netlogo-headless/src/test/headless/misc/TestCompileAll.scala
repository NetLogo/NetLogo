// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import org.nlogo.core.model.ModelReader
import org.nlogo.api.{ FileIO, Version }
import org.nlogo.workspace.ModelsLibrary
import org.scalatest.FunSuite
import org.nlogo.util.SlowTestTag

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
      path.containsSlice("Frogger") || // uses sound extension
      path.containsSlice("Sound Machines") || // uses sound extension
      path.containsSlice("GoGoMonitor") ||
      path.containsSlice("Movie Example") ||
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

class TestCompileAll extends FunSuite  {
  for {
    path <- ModelsLibrary.getModelPaths.filterNot(TestCompileAll.badPath)
    text <- TestCompileAll.goodModel(FileIO.file2String(path))
  }  {
      test("compile: " + path, SlowTestTag) {
        compile(path, text)
      }
      test("readWriteRead: " + path, SlowTestTag) {
        readWriteRead(path, text)
      }
    }

  for(path <- ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions"))
    test("version: " + path, SlowTestTag) {
      val workspace = HeadlessWorkspace.newInstance
      val version = ModelReader.parseModel(FileIO.file2String(path), workspace.parser).version
      assert(Version.compatibleVersion(version))
    }

  def readWriteRead(path: String, text: String) {
    val workspace = HeadlessWorkspace.newInstance
    try {
      val modelContents = text
      val model = ModelReader.parseModel(modelContents, workspace.parser)
      val newModel = ModelReader.parseModel(
        ModelReader.formatModel(model, workspace.parser), workspace.parser)
      assertResult(model.code)(newModel.code)
      assertResult(model.widgets)(newModel.widgets)
      assertResult(model.info)(newModel.info)
      assertResult(model.version)(newModel.version)
      assertResult(model.turtleShapes)(newModel.turtleShapes)
      assertResult(model.behaviorSpace)(newModel.behaviorSpace)
      assertResult(model.linkShapes)(newModel.linkShapes)
      assertResult(model.previewCommands)(newModel.previewCommands)
      assertResult(model)(newModel)
    }
    finally workspace.dispose()
  }

  def compile(path: String, text: String) {
    val workspace = HeadlessWorkspace.newInstance
    // compilerTestingMode keeps patches from being created, which we don't need (and which was
    // slowing things down), and has some other effects too - ST 1/13/05, 12/6/07
    workspace.compilerTestingMode = true
    try {
      System.setProperty("netlogo.extensions.dir", "jvm/extensions")
      workspace.open(path)
      val lab = HeadlessWorkspace.newLab
      lab.load(ModelReader.parseModel(
        text, workspace.parser).behaviorSpace.mkString("", "\n", "\n"))
      lab.names.foreach(lab.newWorker(_).compile(workspace))
    }
    finally workspace.dispose()
  }

}
