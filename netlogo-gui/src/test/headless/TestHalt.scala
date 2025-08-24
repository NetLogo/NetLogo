// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.lang.ref.Cleaner

import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.api.{ LogoException, Version }
import org.nlogo.nvm.HaltException
import org.nlogo.util.SlowTest

class TestHalt extends AnyFunSuite with SlowTest {
  @volatile var finalized = false
  if(!Version.is3D)
    test("halt", SlowTest.Tag) {
      val cleaner = Cleaner.create()
      finalized = false
      var workspace = HeadlessWorkspace.newInstance
      cleaner.register(workspace, new Runnable() {
        override def run(): Unit = {
          finalized = true
        }
      })
      workspace.initForTesting(0, 0, 0, 0, "globals [x]")
      var ex: LogoException = null
      val thread = new Thread("TestHalt.testHalt") {
        override def run(): Unit = {
          try workspace.command("loop [ set x x + 1 ]")
          catch { case e: LogoException => ex = e }
        }
      }
      thread.start()
      def loop(): Unit = {
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
