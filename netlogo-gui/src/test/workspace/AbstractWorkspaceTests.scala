// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{ BeforeAndAfterEach, OneInstancePerTest }

import scala.jdk.CollectionConverters.SeqHasAsJava

class AbstractWorkspaceTests extends AnyFunSuite with BeforeAndAfterEach with OneInstancePerTest {
  val workspace = new DummyAbstractWorkspace
  override def afterEach(): Unit = { workspace.dispose() }
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
    assertResult("/tmp/abc.txt")("[A-Z]:".r.replaceFirstIn(workspace.attachModelDir("abc.txt").replace("\\", "/"), ""))
  }
  test("AttachModelDir2") {
    workspace.setModelPath("/tmp/foo.nlogox")
    assertResult("/usr/abc.txt")("[A-Z]:".r.replaceFirstIn(workspace.attachModelDir("/usr/abc.txt").replace("\\", "/"), ""))
  }
  test("AttachModelDir3") {
    val home = System.getProperty("user.home").replace("\\", "/")
    assertResult(s"$home/abc.txt")(workspace.attachModelDir("abc.txt").replace("\\", "/"))
  }

  // GlobalsIdentifier

  test("GlobalsIdentifier allows BehaviorSpace global variables") {
    workspace.checkGlobalVariable("random-seed", Seq(Double.box(0)).asJava)
    workspace.checkGlobalVariable("min-pxcor", Seq(Double.box(-15)).asJava)
    workspace.checkGlobalVariable("max-pxcor", Seq(Double.box(15)).asJava)
    workspace.checkGlobalVariable("min-pycor", Seq(Double.box(-15)).asJava)
    workspace.checkGlobalVariable("max-pycor", Seq(Double.box(15)).asJava)
    workspace.checkGlobalVariable("world-width", Seq(Double.box(33)).asJava)
    workspace.checkGlobalVariable("world-height", Seq(Double.box(33)).asJava)
  }

  test("GlobalsIdentifier rejects invalid values for BehaviorSpace global variables") {
    assertThrows[Exception](workspace.checkGlobalVariable("random-seed", Seq("test").asJava))
    assertThrows[Exception](workspace.checkGlobalVariable("min-pxcor", Seq("test").asJava))
    assertThrows[Exception](workspace.checkGlobalVariable("max-pxcor", Seq("test").asJava))
    assertThrows[Exception](workspace.checkGlobalVariable("min-pycor", Seq("test").asJava))
    assertThrows[Exception](workspace.checkGlobalVariable("max-pycor", Seq("test").asJava))
    assertThrows[Exception](workspace.checkGlobalVariable("world-width", Seq("test").asJava))
    assertThrows[Exception](workspace.checkGlobalVariable("world-height", Seq("test").asJava))
  }
}
