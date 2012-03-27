// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.{ FunSuite, BeforeAndAfterEach }
import AbstractWorkspace._

class AbstractWorkspaceTests extends FunSuite with BeforeAndAfterEach {
  val workspace = new DummyAbstractWorkspace
  override def afterEach() { workspace.dispose() }
  test("MakeModelNameForDisplay") {
    expect("there")(makeModelNameForDisplay("there.nlogo"))
  }
  test("MakeModelNameForDisplay2") {
    expect("there")(makeModelNameForDisplay("there"))
  }
  test("MakeModelNameForDisplay3") {
    expect("th.ere")(makeModelNameForDisplay("th.ere"))
  }
  test("MakeModelNameForDisplay4") {
    expect("th.ere")(makeModelNameForDisplay("th.ere.nlogo"))
  }
  test("MakeModelNameForDisplay5") {
    expect("foo.nlogo2")(makeModelNameForDisplay("foo.nlogo2"))
  }
  test("MakeModelNameForDisplayWithNullArg") {
    expect("Untitled")(makeModelNameForDisplay(null))
  }
  ///
  test("GuessExportName1") {
    workspace.setModelPath("foo.nlogo")
    expect("foo graphics.png")(workspace.guessExportName("graphics.png"))
  }
  test("GuessExportName2") {
    workspace.setModelPath("fo.o.nlogo")
    expect("fo.o graph.ics.png")(workspace.guessExportName("graph.ics.png"))
  }
  test("GuessExportName3") {
    workspace.setModelPath("foo.nlogo.nlogo")
    expect("foo.nlogo graphics.png")(workspace.guessExportName("graphics.png"))
  }
  test("GuessExportName4") {
    workspace.setModelPath(null)
    expect("graphics.png")(workspace.guessExportName("graphics.png"))
  }
  test("GuessExportName5") {
    workspace.setModelPath("foo")
    expect("foo graphics.png")(workspace.guessExportName("graphics.png"))
  }
}
