// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.util.SlowTest

/**
 * This trait allows you to run model tests in parallel, in N threads.
 * The simplest way to use this trait is to use it just as you would AbstractTestModels.
 * simply call testModel, or testModelFile. This will automatically run
 * your test in parallel in 10 threads.
 */
trait TestMultiThreadedModels extends AbstractTestModels {

  val defaultNumberThreads = 10

  /**
   * run a model test in 10 threads, using the given model
   */
  override def testModel(testName: String, model: Model)(f: => Unit) {
    testModelWithThreads(defaultNumberThreads, testName, model){ f }
  }

  /**
   * run a model test in 10 threads, loading the model from the given file
   */
  override def testModelFile(testName: String, path: String)(f: => Unit) {
    testModelFileWithThreads(defaultNumberThreads, testName, path){ f }
  }

  /**
   * run a model test in N threads, using the given model
   */
  def testModelWithThreads(threads:Int, testName: String, model: Model)(f: => Unit) {
    val is3D = isModel3D(model)
    test(testName, arityTag(is3D), SlowTest.Tag){ testWithThreads(threads){ runModel(is3D, model){ f } } }
  }

  /**
   * run a model test in N threads, loading the model from the given file
   */
  def testModelFileWithThreads(threads:Int, testName: String, path: String)(f: => Unit) {
    val is3D = isModelPath3D(path)
    test(testName, arityTag(is3D), SlowTest.Tag){ testWithThreads(threads){ runModelFromFile(is3D, path){ f } } }
  }

  /**
   * Run the threads, and throw any errors after all threads are completed.
   */
  private def testWithThreads(threadCount:Int)(f: => Unit){
    class MyThread extends Thread("testParallelOperation") {
      var exception: Option[Exception] = None
      override def run() {
        try f
        catch {case e: Exception => exception = Some(e)}
      }
    }
    val threads = (1 to threadCount).toList.map(_ => new MyThread)
    threads.foreach(_.start())
    for (thread <- threads) {
      thread.join()
      thread.exception.foreach(throw _)
    }
  }
}

/**
 * An example of using TestMultiThreadedModels
 * This runs gas lab gas in a box in parallel using 10 threads
 */
class GasLabInParallelTests extends TestMultiThreadedModels with SlowTest {

  testModelFile("gas lab in parallel",
    "models/Sample Models/Chemistry & Physics/GasLab/GasLab Gas in a Box.nlogo"){
    observer >> "random-seed 571  setup  repeat 50 [ go ]"
    reporter("avg-speed") -> 9.760082324073991
  }
}
