// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import org.nlogo.api.Version
import org.nlogo.util.SlowTestTag
import org.nlogo.workspace.Checksummer
import org.scalatest.{ FunSuite, Args, Status, SucceededStatus }

class TestChecksums extends FunSuite  {

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
    test(entry.path, SlowTestTag) {
      val tester = new ChecksumTester(info(_), () => versionMismatchCount += 1)
      tester.testChecksum(entry.path, entry.worldSum, entry.graphicsSum, entry.revision)
      val failures = tester.failures.toString
      if (failures.nonEmpty)
        fail(failures)
    }

  test("not too many version mismatches", SlowTestTag) {
    assert(versionMismatchCount.toDouble / entries.size <= 0.02,
      s"${versionMismatchCount} models have version mismatches (more than 2%)")
  }

}

class ChecksumTester(val info: String => Unit, versionMismatch: () => Unit = () => ()) {

  def addFailure(s: String) = failures.synchronized {failures ++= s}
  val failures = new StringBuilder

  def withWorkspace[T](body: HeadlessWorkspace => T): T = {
    val ws = HeadlessWorkspace.newInstance
    ws.silent = true
    try body(ws)
    finally ws.dispose()
  }

  def testChecksum(model: String, expectedWorldSum: String, expectedGraphicsSum: String, expectedRevision: String) {
    withWorkspace { workspace =>
      val actualRevision = ChecksumsAndPreviews.Checksums.getRevisionNumber(model)
      val revisionMatches = expectedRevision == actualRevision
      if (!revisionMatches) {
        versionMismatch()
        info(s"checksums.txt has $expectedRevision but model is $actualRevision")
      }
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
          return
        }
      }
      new java.io.File("tmp").mkdir()
      new java.io.File("tmp/TestChecksums").mkdir()
      val name = model.substring(
        model.lastIndexOf('/'),
        model.lastIndexOf(".nlogo"))
      workspace.exportView("tmp/TestChecksums/" + name + ".png",
        "PNG")
    }
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
