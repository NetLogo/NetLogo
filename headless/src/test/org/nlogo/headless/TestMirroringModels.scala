// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.FunSuite
import org.nlogo.{ api, mirror }
import org.nlogo.util.SlowTest
import mirror._
import Mirroring._
import Mirrorables._
import TestMirroring.withWorkspace
import org.nlogo.drawing.DrawingActionRunner

class TestMirroringModels extends FunSuite with SlowTest {

  // prevent annoying JAI message on Linux when using JAI extension
  // (old.nabble.com/get-rid-of-%22Could-not-find-mediaLib-accelerator-wrapper-classes%22-td11025745.html)
  System.setProperty("com.sun.media.jai.disableMediaLib", "true")

  def modelRenderingTest(path: String) {
    withWorkspace { (ws, mirrorables) =>
      val drawingActionBuffer = new api.ActionBuffer(ws.drawingActionBroker)

      ws.open(path)
      Checksummer.initModelForChecksumming(ws)

      val (m0, u0) = diffs(Map(), mirrorables())
      var state = Mirroring.merge(
        Map(),
        Serializer.fromBytes(
          Serializer.toBytes(u0)))
      // should I test that m0 and state are identical? maybe have a separate test for that
      val dummy = new FakeWorld(state)
      val renderer = dummy.newRenderer
      renderer.renderLabelsAsRectangles_=(true)

      val runner = new DrawingActionRunner(renderer.trailDrawer)
      drawingActionBuffer.grab().foreach(runner.run)

      val realChecksum =
        Checksummer.calculateGraphicsChecksum(ws.renderer, ws)
      val mirrorChecksum =
        Checksummer.calculateGraphicsChecksum(renderer, ws)

      def exportPNG(r: api.RendererInterface, suffix: String) = {
        new java.io.File("tmp").mkdir()
        val outputFile = new java.io.File(path).getName + "." + suffix + ".png"
        val outputPath = new java.io.File("tmp/" + outputFile)
        javax.imageio.ImageIO.write(r.exportView(ws), "png", outputPath)
      }

      if (mirrorChecksum != realChecksum) {
        exportPNG(ws.renderer, "original")
        exportPNG(renderer, "mirror")
      }

      expectResult(realChecksum) { mirrorChecksum }
    }
  }

  if (!api.Version.is3D) {
    val exclusions = Seq(
      "Diffusion on a Directed Network", // link shapes don't work properly
      "GIS General Examples", // the GIS ext. bypasses the trailDrawer
      "GIS Gradient Example")
    for {
      path <- TestChecksums.checksums.values.map(_.path)
      if !exclusions.exists(name => path.endsWith(name + ".nlogo"))
    } test("Mirroring: " + path) {
      modelRenderingTest(path)
    }
  }
}
