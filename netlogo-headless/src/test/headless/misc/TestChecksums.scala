// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.misc

import java.nio.file.{ Files, Paths }
import java.util.Locale

import org.nlogo.core.I18N
import org.nlogo.headless.ChecksumsAndPreviewsSettings.ChecksumsPath
import org.nlogo.headless.{ ChecksumsAndPreviews, HeadlessWorkspace }
import org.nlogo.util.{ AnyFunSuiteEx, SlowTest }
import org.nlogo.workspace.Checksummer

import org.scalatest.{ Args, Status, SucceededStatus }

class TestChecksums extends ChecksumTester {
  // ensure that output/error messages are formatted as expected (Isaac B 12/28/25)
  I18N.setAllLanguages(Locale.US, false)

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

  ChecksumsAndPreviews.checksumEntries().foreach { entry =>
    test(Paths.get(ChecksumsPath).relativize(entry.checksumPath).toString, SlowTest.Tag) {
      testChecksum(entry)
    }
  }
}

class ChecksumTester extends AnyFunSuiteEx {
  def testChecksum(entry: ChecksumsAndPreviews.Entry): Unit = {
    val workspace = HeadlessWorkspace.newInstance

    workspace.silent = true

    workspace.open(entry.modelPath.toString, true)

    Checksummer.initModelForChecksumming(workspace, entry.variant.getOrElse(""))

    val expectedWorld = Files.readString(entry.checksumPath.resolve("world.csv")).replace("\r\n", "\n")
    val actualWorld = Checksummer.exportWorld(workspace)

    assert(expectedWorld == actualWorld)

    val expectedGraphics = Files.readAllBytes(entry.checksumPath.resolve("graphics.png"))
    val actualGraphics = Checksummer.exportGraphics(workspace)

    if (!expectedGraphics.sameElements(actualGraphics)) {
      val out = Paths.get(s"tmp/TestChecksums/${entry.checksumPath.getFileName}-headless.png")

      out.getParent.toFile.mkdirs()

      Files.write(out, actualGraphics)

      fail("View image did not match expected.")
    }

    workspace.dispose()
  }
}
