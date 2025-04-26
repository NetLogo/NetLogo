// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfterEach
import AbstractWorkspace.makeModelNameForDisplay

class AbstractWorkspaceTests extends AnyFunSuite with BeforeAndAfterEach {
  val workspace = new DummyAbstractWorkspace
  override def afterEach(): Unit = { workspace.dispose() }
  test("MakeModelNameForDisplay") {
    assertResult("there")(makeModelNameForDisplay("there.nlogox"))
  }
  test("MakeModelNameForDisplay2") {
    assertResult("there")(makeModelNameForDisplay("there"))
  }
  test("MakeModelNameForDisplay3") {
    assertResult("th.ere")(makeModelNameForDisplay("th.ere"))
  }
  test("MakeModelNameForDisplay4") {
    assertResult("th.ere")(makeModelNameForDisplay("th.ere.nlogox"))
  }
  test("MakeModelNameForDisplay5") {
    assertResult("foo.nlogo2")(makeModelNameForDisplay("foo.nlogo2"))
  }
  test("MakeModelNameForDisplayWithNullArg") {
    assertResult("Untitled")(makeModelNameForDisplay(null))
  }
  ///
  test("GuessExportName1") {
    workspace.setModelPath("foo.nlogox")
    assertResult("foo graphics.png")(workspace.guessExportName("graphics.png"))
  }
  test("GuessExportName2") {
    workspace.setModelPath("fo.o.nlogox")
    assertResult("fo.o graph.ics.png")(workspace.guessExportName("graph.ics.png"))
  }
  test("GuessExportName3") {
    workspace.setModelPath("foo.nlogo.nlogox")
    assertResult("foo.nlogo graphics.png")(workspace.guessExportName("graphics.png"))
  }
  test("GuessExportName4") {
    workspace.setModelPath(null)
    assertResult("graphics.png")(workspace.guessExportName("graphics.png"))
  }
  test("GuessExportName5") {
    workspace.setModelPath("foo")
    assertResult("foo graphics.png")(workspace.guessExportName("graphics.png"))
  }
}
