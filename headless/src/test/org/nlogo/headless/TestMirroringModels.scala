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
import scala.sys.process.stringSeqToProcess
import collection.JavaConverters._

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

      def pngFileName(suffix: String): String =
        "tmp/" + new java.io.File(path).getName + "." + suffix + ".png"

      def exportPNG(r: api.RendererInterface, fileName: String): Unit = {
        javax.imageio.ImageIO.write(r.exportView(ws), "png", new java.io.File(fileName))
      }

      if (mirrorChecksum != realChecksum) {
        new java.io.File("tmp").mkdir() // make sure tmp/ exists
        val originalFN = pngFileName("original")
        exportPNG(ws.renderer, originalFN)
        val mirrorFN = pngFileName("mirror")
        exportPNG(renderer, mirrorFN)
        // create diff (requires ImageMagick)
        val diffFN = pngFileName("diff")
        Seq("compare", originalFN, mirrorFN, diffFN).!
      }

      assertResult(realChecksum) { mirrorChecksum }
    }
  }

  if (!api.Version.is3D) {
    val exclusions = Seq(
      "GIS General Examples", // the GIS ext. bypasses the trailDrawer
      "GIS Gradient Example",
      "Ticket Sales")
    for {
      path <- TestChecksums.checksums.values.map(_.path)
      if !exclusions.exists(name => path.endsWith(name + ".nlogo"))
    } test("Mirroring: " + path) {
      System.out.println(path)
      modelRenderingTest(path)
    }
  }
}

/**
 * This test helped me diagnose a problem with the rendering of links in
 * the "Diffusion on a Directed Network" model. It turned out that the order
 * of links in fake breed agentsets was wrong. The test is currently specific
 * to this model, but it can't hurt to leave it there (and it could be
 * generalised if needed.) NP 2014-01-13
 */
class TestDiffusionOnDirectedNetworkMirroring extends FunSuite {
  if (!api.Version.is3D)
    test("TestDiffusionOnDirectedNetworkMirroring") {
      val path = TestChecksums.checksums.values.map(_.path)
        .find(_.contains("Diffusion on a Directed Network")).get
      withWorkspace { (ws, mirrorables) =>
        ws.open(path)
        Checksummer.initModelForChecksumming(ws)
        val mirroredState = Mirroring.merge(Map(), diffs(Map(), mirrorables())._2)
        val fakeWorld = new FakeWorld(mirroredState)
        def getLinks(world: api.World, breedName: String) = world
          .getLinkBreed(breedName).agents.asScala
          .collect { case l: api.Link => l }
        for {
          breedName <- ws.world.program.breeds.keys
          fakeLinks = getLinks(fakeWorld, breedName)
          realLinks = getLinks(ws.world, breedName)
          _ = assertResult(realLinks.size)(fakeLinks.size)
          (rl, fl) <- (realLinks, fakeLinks).zipped
        } {
          assertResult(rl.end1.id)(fl.end1.id)
          assertResult(rl.end2.id)(fl.end2.id)
          assertResult(rl.color)(fl.color)
        }
      }
    }
}
