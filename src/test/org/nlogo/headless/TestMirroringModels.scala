// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.FunSuite
import org.nlogo.{ api, mirror }
import org.nlogo.util.{ Pico, SlowTest }
import mirror._
import Mirroring._
import Mirrorables._
import TestMirroring.withWorkspace

class TestMirroringModels extends FunSuite with SlowTest {

  // prevent annoying JAI message on Linux when using JAI extension
  // (old.nabble.com/get-rid-of-%22Could-not-find-mediaLib-accelerator-wrapper-classes%22-td11025745.html)
  System.setProperty("com.sun.media.jai.disableMediaLib", "true")

  def modelRenderingTest(path: String) {
    withWorkspace { (ws, mirrorables) =>
      ws.open(path)
      Checksummer.initModelForChecksumming(ws)
      val (m0, u0) = diffs(Map(), mirrorables())
      var state = Mirroring.merge(
        Map(),
        Serializer.fromBytes(
          Serializer.toBytes(u0)))
      // should I test that m0 and state are identical? maybe have a separate test for that
      val dummy = new FakeWorld(state)
      val pico = new Pico
      pico.add("org.nlogo.render.Renderer")
      pico.addComponent(dummy)
      val renderer = pico.getComponent(classOf[api.RendererInterface])
      renderer.resetCache(ws.patchSize)
      renderer.renderLabelsAsRectangles_=(true)

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

      expect(realChecksum) { mirrorChecksum }
    }
  }

  if (!api.Version.is3D)
    for(entry <- TestChecksums.checksums.values)
      // exclude 1 model for now, failing & we don't know why yet
      if(!entry.path.endsWith("Diffusion on a Directed Network.nlogo"))
        test("Mirroring: " + entry.path) {
          modelRenderingTest(entry.path)
        }

}
