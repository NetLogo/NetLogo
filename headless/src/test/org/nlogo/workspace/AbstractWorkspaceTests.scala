// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.{ FunSuite, BeforeAndAfterEach }
import AbstractWorkspace._

class AbstractWorkspaceTests extends FunSuite with BeforeAndAfterEach {
  val workspace = new DummyAbstractWorkspace
  override def afterEach() { workspace.dispose() }
  test("MakeModelNameForDisplay") {
    expectResult("there")(makeModelNameForDisplay("there.nlogo"))
  }
  test("MakeModelNameForDisplay2") {
    expectResult("there")(makeModelNameForDisplay("there"))
  }
  test("MakeModelNameForDisplay3") {
    expectResult("th.ere")(makeModelNameForDisplay("th.ere"))
  }
  test("MakeModelNameForDisplay4") {
    expectResult("th.ere")(makeModelNameForDisplay("th.ere.nlogo"))
  }
  test("MakeModelNameForDisplay5") {
    expectResult("foo.nlogo2")(makeModelNameForDisplay("foo.nlogo2"))
  }
  test("MakeModelNameForDisplayWithNullArg") {
    expectResult("Untitled")(makeModelNameForDisplay(null))
  }
  ///
  test("GuessExportName1") {
    workspace.setModelPath("foo.nlogo")
    expectResult("foo graphics.png")(workspace.guessExportName("graphics.png"))
  }
  test("GuessExportName2") {
    workspace.setModelPath("fo.o.nlogo")
    expectResult("fo.o graph.ics.png")(workspace.guessExportName("graph.ics.png"))
  }
  test("GuessExportName3") {
    workspace.setModelPath("foo.nlogo.nlogo")
    expectResult("foo.nlogo graphics.png")(workspace.guessExportName("graphics.png"))
  }
  test("GuessExportName4") {
    workspace.setModelPath(null)
    expectResult("graphics.png")(workspace.guessExportName("graphics.png"))
  }
  test("GuessExportName5") {
    workspace.setModelPath("foo")
    expectResult("foo graphics.png")(workspace.guessExportName("graphics.png"))
  }
}
