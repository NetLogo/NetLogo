// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version
import org.nlogo.workspace.Checksummer
import java.util.concurrent.{Executors, TimeUnit}
import org.nlogo.util.SlowTest
import org.scalatest._

import scala.language.implicitConversions

class TestChecksums extends FunSuite {

  // overriding this so we can pass in a model filter to run checksums against a single model.
  // example   sbt> checksums model=Echo
  override def run(testName: Option[String], args: Args) = {
    val allTests: Set[String] = testNames
    val selection = args.configMap.get("model")
    val testsToRun = allTests.filter((tname: String) => selection.map(tname.contains(_)).getOrElse(true))
    val allOtherTests = allTests -- testsToRun
    val ignoreAllOtherTests = allOtherTests.map(_ -> Set("org.scalatest.Ignore")).toMap
    val ignoreAllOtherTestsInThisSuite = Map(suiteId -> ignoreAllOtherTests)
    val newDynatags = DynaTags(args.filter.dynaTags.suiteTags, args.filter.dynaTags.testTags ++ ignoreAllOtherTestsInThisSuite)
    val newFilter = Filter(args.filter.tagsToInclude, args.filter.tagsToExclude, args.filter.excludeNestedSuites, newDynatags)
    super.run(testName, args.copy(filter = newFilter))
  }

  // prevent annoying JAI message on Linux when using JAI extension
  // (old.nabble.com/get-rid-of-%22Could-not-find-mediaLib-accelerator-wrapper-classes%22-td11025745.html)
  System.setProperty("com.sun.media.jai.disableMediaLib", "true")
  // We're disabling nw and ls extension models from getting checksummed due to the fact that they're inconsistent at the moment
  // this MUST BE FIXED - RG 8/31/16
  for (entry <- TestChecksums.checksums.values if ! entry.path.contains("Examples/nw") && ! entry.path.contains("Examples/ls")) {
    test(entry.path, SlowTest.Tag) {
      val tester = new ChecksumTester(info(_))
      tester.testChecksum(entry.path, entry.worldSum, entry.graphicsSum, entry.revision)
      val failures = tester.failures.toString
      if (failures.nonEmpty)
        fail(failures)
    }
  }
}

object TestChecksums extends ChecksumTester(println _) {
  def checksums = {
    val path = if (Version.is3D) "test/checksums3d.txt"
               else "test/checksums.txt"
    ChecksumsAndPreviews.Checksums.load(path)
  }

  def main(args: Array[String]) {

    val runTimes = new scala.collection.parallel.mutable.ParHashMap[String, Long]
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)

    val checksums = this.checksums

    implicit def thunk2runnable(fn: () => Unit): Runnable = new Runnable {def run() {fn()}}

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
      executor.execute(thunk2runnable(doit _))
    }
    executor.shutdown()
    executor.awaitTermination(java.lang.Integer.MAX_VALUE, TimeUnit.SECONDS)
    // print report of longest runtimes (so we can alter preview commands to not take so long)
    val n = 30
    println(n + " slowest models:")
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
    val actual2 = Checksummer.calculateGraphicsChecksum(workspace)
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
