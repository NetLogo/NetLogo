// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.FunSuite
import org.nlogo.api.{ LogoException, RendererInterface, Version }
import org.nlogo.agent.World
import org.nlogo.nvm.{ CompilerInterface, HaltException }
import org.nlogo.util.SlowTest

object TestHalt {
  // This is ugly, but since we use PicoContainer to instantiate HeadlessWorkspace it's hard to
  // subclass.  Oh well, this is only test code. - ST 3/4/09
  var finalized = false
  class MyWorkspace(world: World, compiler: CompilerInterface, renderer: RendererInterface)
  extends HeadlessWorkspace(world, compiler, renderer) {
    override def finalize() { finalized = true; super.finalize() }
  }
}
class TestHalt extends FunSuite with SlowTest {
  test("halt") {
    import TestHalt._
    finalized = false
    var workspace =
      HeadlessWorkspace.newInstance(classOf[MyWorkspace]).asInstanceOf[MyWorkspace]
    workspace.initForTesting(0, 0, 0, 0, "globals [x]")
    var ex: LogoException = null
    val thread = new Thread("TestHalt.testHalt") {
      override def run() {
        try workspace.command("loop [ set x x + 1 ]")
        catch { case e: LogoException => ex = e }
      }
    }
    thread.start()
    def loop() {
      if (ex != null) throw ex
      if (workspace.report("x").asInstanceOf[Double] < 10) {
        Thread.sleep(5)
        loop()
      }
    }
    loop()
    workspace.halt()
    workspace.dispose()
    workspace = null
    thread.join()
    if (ex != null)
      assert(ex.isInstanceOf[HaltException])
    System.gc()
    for (i <- 1 to 50)
      if (!finalized) { Thread.sleep(200); System.gc() }
    assert(finalized)
  }
}
