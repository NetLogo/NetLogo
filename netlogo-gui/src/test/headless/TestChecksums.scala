// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.io.File
import java.util.concurrent.{ Executors, TimeUnit }

import org.nlogo.util.{ AnyFunSuiteEx, SlowTest }
import org.nlogo.workspace.Checksummer

import org.scalatest.{ Args, Status, SucceededStatus }

import scala.language.implicitConversions

class TestChecksums extends AnyFunSuiteEx {

  // overriding this so we can pass in a model filter to run checksums against a single model.
  // example   sbt> checksums model=Echo
  override def runTest(testName: String, args: Args): Status = {
    val shouldRun =
      args.configMap.get("model")
        .collect{case s: String => s}
        .forall(testName.containsSlice(_))
    if(shouldRun)
      super.runTest(testName, args)
    else
      SucceededStatus
  }

  // prevent annoying JAI message on Linux when using JAI extension
  // (old.nabble.com/get-rid-of-%22Could-not-find-mediaLib-accelerator-wrapper-classes%22-td11025745.html)
  System.setProperty("com.sun.media.jai.disableMediaLib", "true")
  // We're disabling nw and ls extension models from getting checksummed due to the fact that they're inconsistent at the moment
  // this MUST BE FIXED - RG 8/31/16
  for (entry <- TestChecksums.checksums.values if ! entry.path.contains("Examples/nw") && ! entry.path.contains("Examples/ls")) {
    test(entry.key, SlowTest.Tag) {
      val tester = new ChecksumTester(info(_))
      tester.testChecksum(entry.path, entry.variant, entry.worldSum, entry.graphicsSum, entry.revision)
      val failures = tester.failures.toString
      if (failures.nonEmpty)
        fail(failures)
    }
  }
}

object TestChecksums extends ChecksumTester(println) {
  val checksums = ChecksumsAndPreviews.Checksums.load()

  def main(args: Array[String]): Unit = {

    val runTimes = new scala.collection.parallel.mutable.ParHashMap[String, Long]
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)

    val checksums = this.checksums

    implicit def thunk2runnable(fn: () => Unit): Runnable = new Runnable {def run(): Unit = {fn()}}

    for (entry <- checksums.values) {
      def doit(): Unit = {
        try {
          print(".")
          runTimes.update(entry.path, System.currentTimeMillis)
          // for now, store current time; we'll replace it later with the difference
          // between this value and the time then - ST 3/27/08
          testChecksum(entry.path, entry.variant, entry.worldSum, entry.graphicsSum, entry.revision)
          runTimes.update(entry.path, System.currentTimeMillis - runTimes(entry.path))
        }
        catch {
          case t: Throwable =>
            println(entry.path + ": ")
            t.printStackTrace()
            addFailure(entry.path + ": " + t.getMessage)
        }
      }
      executor.execute(thunk2runnable(doit))
    }
    executor.shutdown()
    executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS)
    // print report of longest runtimes (so we can alter preview commands to not take so long)
    val n = 30
    println(s"$n slowest models:")
    val keys = checksums.keySet.toSeq.sortBy(runTimes.apply).reverse.take(n)
    for (key <- keys)
      println("  " + key + " " + runTimes(key) / 1000 + " seconds")
    // done, whole test fails if any model failed
    if (failures.toString.nonEmpty)
      println("Test Failed!")

  }
}

class ChecksumTester(info: String => Unit) {
  def addFailure(s: String) = failures.synchronized {failures ++= s}
  // I copied and pasted the thread pool stuff from TestCompileAll.  If I ever make a third copy,
  // it's time to abstract - ST 2/12/09  maybe using ExecutorServices.invokeAll - ST 3/29/09
  val failures = new StringBuilder

  def testChecksum(model: String, variant: String, expectedWorldSum: String, expectedGraphicsSum: String, revision: String): Unit = {
    val workspace = HeadlessWorkspace.newInstance
    workspace.silent = true
    workspace.open(model, true)
    Checksummer.initModelForChecksumming(workspace, variant)
    val actual = Checksummer.calculateWorldChecksum(workspace)
    if (expectedWorldSum != actual) {
      val message = model + "\n  expected world checksum " + expectedWorldSum + "\n  but got " + actual + "\n"
      addFailure(message)
      info("\n" + message)
      exportWorld(workspace, model)
    }
    // test view contents checksum
    val actual2 = Checksummer.calculateGraphicsChecksum(workspace)
    if (expectedGraphicsSum != actual2) {
      val message = model + "\n  expected graphics checksum " + expectedGraphicsSum + "\n  but got " + actual2 + "\n"
      addFailure(message)
      info("\n" + message)
    }
    new File("tmp").mkdir()
    new File("tmp/TestChecksums").mkdir()
    workspace.exportView("tmp/TestChecksums/" + model.substring(model.lastIndexOf('/'), model.lastIndexOf(".nlogox")) + ".png",
      "PNG")
    workspace.dispose()
  }

  def exportWorld(workspace: HeadlessWorkspace, model: String): Unit = {
    new File("tmp").mkdir()
    new File("tmp/TestChecksums").mkdir()
    val path = "tmp/TestChecksums/" + new File(model).getName + ".csv"
    info("  exporting world to " + path)
    try workspace.exportWorld(path)
    catch {
      case t: Throwable => t.printStackTrace; throw t
    }
  }

}
