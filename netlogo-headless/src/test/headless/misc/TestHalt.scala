// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import java.lang.ref.Cleaner

import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.util.SlowTest
import org.nlogo.{ core, api, agent, nvm }

object TestHalt {
  // This is ugly, but since we use reflection to instantiate HeadlessWorkspace it's hard to
  // subclass.  Oh well, this is only test code. - ST 3/4/09
  class MyWorkspace(world: agent.World, compiler: nvm.CompilerInterface,
    renderer: api.RendererInterface)
  extends HeadlessWorkspace(world, compiler, renderer)
}

class TestHalt extends AnyFunSuite  {
  @volatile var finalized = false
  // I've had weird Heisenbug-type problems with the workspace not getting GC'ed if
  // it's a local variable rather than a top-level class member - ST 1/8/13
  var workspace: HeadlessWorkspace = null

  def withWorkspace(body: => Unit): Unit = {
    import TestHalt._
    val cleaner = Cleaner.create()
    finalized = false
    workspace = HeadlessWorkspace.newInstance(classOf[MyWorkspace])
    cleaner.register(workspace, new Runnable() {
      override def run(): Unit = {
        finalized = true
      }
    })
    body
    workspace.halt()
    workspace.dispose()
    workspace = null
    for (i <- 1 to 20)
      if (!finalized) { Thread.sleep(200); System.gc() }
    assert(finalized)
  }

  test("halt 0", SlowTest.Tag) {
    withWorkspace { }
  }

  test("halt 1", SlowTest.Tag) {
    withWorkspace {
      // multiply possible memory leaks
      workspace.compileCommands("")
    }
  }

  test("halt 2", SlowTest.Tag) {
    var ex: api.LogoException = null
    val thread = new Thread("TestHalt.testHalt") {
      override def run(): Unit = {
        try workspace.command("loop [ set x x + 1 ]")
        catch { case e: api.LogoException => ex = e }
      }
    }
    withWorkspace {
      workspace.openModel(core.Model(code = "globals [x]"))
      thread.start()
      def loop(): Unit = {
        if (ex != null) throw ex
        if (workspace.report("x").asInstanceOf[Double] < 10) {
          Thread.sleep(5)
          loop()
        }
      }
      loop()
    }
    thread.join()
    if (ex != null)
      assert(ex.isInstanceOf[nvm.HaltException])
  }

}
