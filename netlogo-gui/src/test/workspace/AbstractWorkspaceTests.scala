// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{ BeforeAndAfterEach, OneInstancePerTest }

class AbstractWorkspaceTests extends AnyFunSuite with BeforeAndAfterEach with OneInstancePerTest {
  val workspace = new DummyAbstractWorkspace
  override def afterEach() { workspace.dispose() }
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
  test("AttachModelDir1") {
    workspace.setModelPath("/tmp/foo.nlogox")
    assertResult("/tmp/abc.txt")(workspace.attachModelDir("abc.txt"))
  }
  test("AttachModelDir2") {
    workspace.setModelPath("/tmp/foo.nlogox")
    assertResult("/usr/abc.txt")(workspace.attachModelDir("/usr/abc.txt"))
  }
  test("AttachModelDir3") {
    val home = System.getProperty("user.home")
    assertResult(s"$home/abc.txt")(workspace.attachModelDir("abc.txt"))
  }
}
