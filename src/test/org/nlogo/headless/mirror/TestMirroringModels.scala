// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package mirror

import org.scalatest.FunSuite
import org.nlogo.{ api, mirror }
import org.nlogo.util.SlowTest
import mirror._
import Mirroring._
import TestMirroring.withWorkspace
import org.nlogo.drawing.DrawingActionRunner
import org.nlogo.workspace.Checksummer

class TestMirroringModels extends FunSuite with SlowTest {

  // prevent annoying JAI message on Linux when using JAI extension
  // (old.nabble.com/get-rid-of-%22Could-not-find-mediaLib-accelerator-wrapper-classes%22-td11025745.html)
  System.setProperty("com.sun.media.jai.disableMediaLib", "true")

  def modelRenderingTest(path: String) {
    withWorkspace { (ws, mirrorables) =>
      val drawingActionBuffer = new api.ActionBuffer(ws.drawingActionBroker)
      drawingActionBuffer.activate()

      ws.open(path)
      Checksummer.initModelForChecksumming(ws)

      val (_, u0) = diffs(Map(), mirrorables())
      val state = Mirroring.merge(
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
        Checksummer.calculateGraphicsChecksum(ws)
      val mirrorChecksum =
        Checksummer.calculateGraphicsChecksum(ws)

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

  val exclusions = Seq(
    "Diffusion on a Directed Network", // link shapes don't work properly
    "Link Breeds Example", // link shapes don't work properly
    "GIS General Examples", // the GIS ext. bypasses the trailDrawer
    "GIS Gradient Example",
    "Movie Example")

  // exclude features not existing on core branch
  val moreExclusions = Seq("/GIS/", "/System Dynamics/")

  def checksums =
    ChecksumsAndPreviews.Checksums.load("test/checksums.txt")

  for {
    path <- checksums.values.map(_.path)
    if !exclusions.exists(name => path.endsWith(name + ".nlogo"))
    if !moreExclusions.exists(name => path.containsSlice(name))
  } test("Mirroring: " + path) {
    modelRenderingTest(path)
  }

}
