// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version
import java.util.concurrent.{Executors, TimeUnit}
import org.nlogo.util.SlowTest
import org.scalatest._

class TestChecksums extends FunSuite with SlowTest {

  // overriding this so we can pass in a model filter to run checksums against a single model.
  // example   sbt> checksums model=Echo
  override def runTest (testName : java.lang.String, reporter : Reporter, stopper : Stopper,
                        configMap : scala.collection.immutable.Map[java.lang.String, Any], tracker : Tracker){
    val shouldRun = configMap.get("model").map(testName contains _.asInstanceOf[String]).getOrElse(true)
    if(shouldRun) super.runTest(testName, reporter, stopper, configMap, tracker)
  }

  // prevent annoying JAI message on Linux when using JAI extension
  // (old.nabble.com/get-rid-of-%22Could-not-find-mediaLib-accelerator-wrapper-classes%22-td11025745.html)
  System.setProperty("com.sun.media.jai.disableMediaLib", "true")
  for {
    entry <- TestChecksums.checksums.values
    if !entry.path.containsSlice("/GIS/")
    if !entry.path.containsSlice("/System Dynamics/")
    if !entry.path.containsSlice("Movie Example")
  } {
    test(entry.path) {
      val tester = new ChecksumTester(info(_))
      tester.testChecksum(entry.path, entry.worldSum, entry.graphicsSum, entry.revision)
      val failures = tester.failures.toString
      if (failures.nonEmpty)
        fail(failures)
    }
  }
}

object TestChecksums extends ChecksumTester(println _) {

  def checksums =
    ChecksumsAndPreviews.Checksums.load("models/test/checksums.txt")

  def main(args: Array[String]) {

    val runTimes = new collection.mutable.HashMap[String, Long]
            with collection.mutable.SynchronizedMap[String, Long]
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)

    val checksums = this.checksums

    for (entry <- checksums.values) {
      def doit() {
        try {
          print(".")
          runTimes.update(entry.path, System.currentTimeMillis)
          // for now, store current time; we'll replace it later with the difference
          // between this value and the time then - ST 3/27/08
          testChecksum(entry.path, entry.worldSum, entry.graphicsSum, entry.revision)
          runTimes.update(entry.path, System.currentTimeMillis - runTimes(entry.path))
        }
        catch {
          case t: Throwable =>
            println(entry.path + ": ")
            t.printStackTrace()
            addFailure(entry.path + ": " + t.getMessage)
        }
      }
      implicit def thunk2runnable(fn: () => Unit): Runnable = new Runnable {def run() {fn()}}
      executor.execute(doit _)
    }
    executor.shutdown()
    executor.awaitTermination(java.lang.Integer.MAX_VALUE, TimeUnit.SECONDS)
    // print report of longest runtimes (so we can alter preview commands to not take so long)
    val n = 30
    println(n + " slowest models:")
    val keys = checksums.keySet.toSeq.sortBy(runTimes).reverse.take(n)
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

  def testChecksum(model: String, expectedWorldSum: String, expectedGraphicsSum: String, revision: String) {
    val workspace = HeadlessWorkspace.newInstance
    workspace.silent = true
    val revisionMatches = revision == ChecksumsAndPreviews.Checksums.getRevisionNumber(model)
    workspace.open(model)
    Checksummer.initModelForChecksumming(workspace)
    val actual = Checksummer.calculateWorldChecksum(workspace)
    if (expectedWorldSum != actual) {
      val message = model + "\n  expected world checksum " + expectedWorldSum + "\n  but got " + actual + "\n"
      if (revisionMatches) {
        addFailure(message)
        info("\n" + message)
        exportWorld(workspace, model)
      }
      else {
        info("version mismatch, ignoring: " + message)
        workspace.dispose()
        return
      }
    }
    // test view contents checksum
    val actual2 = Checksummer.calculateGraphicsChecksum(workspace.renderer, workspace)
    if (expectedGraphicsSum != actual2) {
      val message = model + "\n  expected graphics checksum " + expectedGraphicsSum + "\n  but got " + actual2 + "\n"
      if (revisionMatches) {
        addFailure(message)
        info("\n" + message)
      }
      else {
        info("version mismatch, ignoring: " + message)
        workspace.dispose()
        return
      }
    }
    new java.io.File("tmp").mkdir()
    new java.io.File("tmp/TestChecksums").mkdir()
    workspace.exportView("tmp/TestChecksums/" + model.substring(model.lastIndexOf('/'), model.lastIndexOf(".nlogo")) + ".png",
      "PNG")
    workspace.dispose()
  }

  def exportWorld(workspace: HeadlessWorkspace, model: String) {
    new java.io.File("tmp").mkdir()
    new java.io.File("tmp/TestChecksums").mkdir()
    val path = "tmp/TestChecksums/" + new java.io.File(model).getName + ".csv"
    info("  exporting world to " + path)
    try workspace.exportWorld(path)
    catch {
      case t: Throwable => t.printStackTrace; throw t
    }
  }

}
