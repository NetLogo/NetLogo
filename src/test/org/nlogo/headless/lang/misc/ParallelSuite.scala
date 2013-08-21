// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.scalatest.FunSuite

/**
 * This trait allows you to run model tests in parallel, in N threads.
 * The simplest way to use this trait is to use it just as you would AbstractTestModels.
 * simply call testModel, or testModelFile. This will automatically run
 * your test in parallel in 10 threads.
 */
trait ParallelSuite extends FunSuite {

  /**
   * Run the threads, and throw any errors after all threads are completed.
   */
  def openParallel(path: String, threadCount: Int = 10)(f: Fixture => Unit){
    class MyThread extends Thread("testParallelOperation") {
      var exception: Option[Exception] = None
      override def run() {
        Fixture.withFixture(path){implicit fixture =>
          fixture.workspace.open(path)
          try f(fixture)
          catch {case e: Exception => exception = Some(e)}
        }
      }
    }
    val threads = Seq.fill(threadCount)(new MyThread)
    threads.foreach(_.start())
    for (thread <- threads) {
      thread.join()
      thread.exception.foreach(throw _)
    }
  }
}
