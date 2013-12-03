// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import org.nlogo.api.Version
import org.nlogo.util.SlowTest
import org.nlogo.workspace.Checksummer
import org.scalatest.{ FunSuite, Args, Status, SucceededStatus }

class TestChecksums extends FunSuite with SlowTest {

  // overriding this so we can pass in a model filter to run checksums against one
  // model, or a subset. example:
  //   testOnly org.nlogo.headless.misc.TestChecksums -- -Dmodel=GenDrift
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

  def skip(path: String): Boolean =
    Seq("/GIS/", "/System Dynamics/", "Movie Example")
      .exists(path.containsSlice(_))

  for(entry <- ChecksumsAndPreviews.Checksums.load().values)
    if (!skip(entry.path))
      test(entry.path) {
        val tester = new ChecksumTester(info(_))
        tester.testChecksum(entry.path, entry.worldSum, entry.graphicsSum, entry.revision)
        val failures = tester.failures.toString
        if (failures.nonEmpty)
          fail(failures)
      }

}

class ChecksumTester(info: String => Unit) {

  def addFailure(s: String) = failures.synchronized {failures ++= s}
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
      case t: Throwable =>
        t.printStackTrace
        throw t
    }
  }

}
