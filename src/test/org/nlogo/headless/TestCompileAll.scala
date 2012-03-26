// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version
import org.nlogo.workspace.ModelsLibrary
import org.scalatest.FunSuite
import org.nlogo.util.SlowTest

class TestCompileAll extends FunSuite with SlowTest{

  for (path <- ModelsLibrary.getModelPaths ++ ModelsLibrary.getModelPathsAtRoot("extensions")) {
    test("compile: " + path) {
      compile(path)
    }
  }

  def compile(path: String) {
    import java.io.File.separatorChar
    def pathMatches(bad: String) =
      path.toUpperCase.containsSlice(separatorChar + bad + separatorChar)
    if (pathMatches("DOESN'T COMPILE") ||
        // letting the textbook team deal with these should help ensure
        // the book gets updated too - ST 4/22/10
        pathMatches("TEXTBOOK MODELS") ||
        // core branch doesn't have these features - ST 1/11/12
        pathMatches("SYSTEM DYNAMICS") ||
        pathMatches("GIS") ||
        pathMatches("QUICKTIME EXTENSION") ||
        pathMatches("HUBNET ACTIVITIES") ||
        path.containsSlice("GoGoMonitor") ||
        path.containsSlice("Movie Example"))
      return
    val workspace = HeadlessWorkspace.newInstance
    // this keeps patches from being created, which we don't need,
    // and which was slowing things down - ST 1/13/05
    // and has some other effects - ST 12/6/07
    workspace.compilerTestingMode = true
    try {
      workspace.open(path)
      // compile BehaviorSpace experiments
      val lab = HeadlessWorkspace.newLab
      lab.load(HeadlessModelOpener.protocolSection(path))
      lab.names.foreach(lab.newWorker(_).compile(workspace))
    }
    finally {workspace.dispose()}
  }

}
