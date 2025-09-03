// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.misc

import java.io.File

import org.nlogo.headless.{ ChecksumsAndPreviews, HeadlessWorkspace }
import org.nlogo.util.SlowTest
import org.nlogo.workspace.Checksummer

import org.scalatest.{ Args, Status, SucceededStatus }
import org.scalatest.funsuite.AnyFunSuite

class TestChecksums extends AnyFunSuite  {

  // overriding this so we can pass in a model filter to run checksums against one
  // model, or a subset. example: `ts GenDrift`
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

  val entries = ChecksumsAndPreviews.Checksums.load().values
  var versionMismatchCount = 0

  for(entry <- entries)
    test(entry.key, SlowTest.Tag) {
      val tester = new ChecksumTester(info(_), () => versionMismatchCount += 1)
      tester.testChecksum(entry.path, entry.variant, entry.worldSum, entry.graphicsSum, entry.revision)
      val failures = tester.failures.toString
      if (failures.nonEmpty)
        fail(failures)
    }

  test("not too many version mismatches", SlowTest.Tag) {
    assert(versionMismatchCount.toDouble / entries.size <= 0.02,
      s"${versionMismatchCount} models have version mismatches (more than 2%)")
  }

}

class ChecksumTester(val info: String => Unit, versionMismatch: () => Unit = () => ()) {

  def addFailure(s: String) = failures.synchronized {failures ++= s}
  val failures = new StringBuilder

  def testChecksum(model: String, variant: String, expectedWorldSum: String, expectedGraphicsSum: String, expectedRevision: String): Unit = {
    val workspace = HeadlessWorkspace.newInstance
    workspace.silent = true
    val actualRevision = ChecksumsAndPreviews.Checksums.getRevisionNumber(model)
    val revisionMatches = expectedRevision == actualRevision
    if (!revisionMatches) {
      versionMismatch()
      info(s"checksums.txt has $expectedRevision but model is $actualRevision")
    }
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
    val name = model.substring(
      model.lastIndexOf('/'),
      model.lastIndexOf(".nlogox"))
    workspace.exportView("tmp/TestChecksums/" + name + ".png",
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
      case t: Throwable =>
        t.printStackTrace
        throw t
    }
  }

}
