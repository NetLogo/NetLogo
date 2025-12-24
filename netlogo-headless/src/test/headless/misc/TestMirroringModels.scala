// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import java.io.File
import javax.imageio.ImageIO

import org.nlogo.{ api, mirror }
import org.nlogo.drawing.DrawingActionRunner
import org.nlogo.util.{ AnyFunSuiteEx, SlowTest }
import org.nlogo.workspace.Checksummer

import mirror._, Mirroring._, Mirrorables._

class TestMirroringModels extends AnyFunSuiteEx  {

  def withWorkspace[T](body: (HeadlessWorkspace, () => Iterable[Mirrorable]) => T): T = {
    val ws = HeadlessWorkspace.newInstance
    ws.silent = true
    try body(ws, () => allMirrorables(ws.world))
    finally ws.dispose()
  }

  // prevent annoying JAI message on Linux when using JAI extension
  // (old.nabble.com/get-rid-of-%22Could-not-find-mediaLib-accelerator-wrapper-classes%22-td11025745.html)
  System.setProperty("com.sun.media.jai.disableMediaLib", "true")

  def modelRenderingTest(path: String): Unit = {
    withWorkspace { (ws, mirrorables) =>
      val drawingActionBuffer = new api.ActionBuffer(ws.drawingActionBroker)
      drawingActionBuffer.activate()

      ws.open(path)
      Checksummer.initModelForChecksumming(ws, "")

      val (_, u0) = diffs(Map(), mirrorables())
      val state = Mirroring.merge(
        Map(),
        Serializer.fromBytes(
          Serializer.toBytes(u0)))
      // should I test that m0 and state are identical? maybe have a separate test for that
      val dummy = new FakeWorld(state)
      val renderer = dummy.newRenderer
      renderer.setRenderLabelsAsRectangles(true)

      val runner = new DrawingActionRunner(renderer.trailDrawer)
      drawingActionBuffer.grab().foreach(runner.run)

      val realChecksum =
        Checksummer.calculateGraphicsChecksum(ws)
      val mirrorChecksum =
        Checksummer.calculateGraphicsChecksum(renderer.exportView(ws))

      def exportPNG(r: api.RendererInterface, suffix: String) = {
        new File("tmp").mkdir()
        val outputFile = new File(path).getName + "." + suffix + ".png"
        val outputPath = new File("tmp/" + outputFile)
        ImageIO.write(r.exportView(ws), "png", outputPath)
      }

      if (mirrorChecksum != realChecksum) {
        exportPNG(ws.renderer, "original")
        exportPNG(renderer, "mirror")
      }

      assertResult(realChecksum)(mirrorChecksum)
    }
  }

  ChecksumsAndPreviews.checksumEntries().map(_.modelPath).distinct.foreach { path =>
    test(s"Mirroring: $path", SlowTest.Tag) {
      modelRenderingTest(path.toString)
    }
  }

}
