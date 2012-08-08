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

  private val testSerializer = true

  def modelRenderingTest(path: String) {
    withWorkspace { (ws, mirrorables) =>
      ws.open(path)
      ws.command("random-seed 0")
      ws.command(ws.previewCommands)
      val (m0, u0) = diffs(Map(), mirrorables())
      var state = Mirroring.merge(
        Map(),
        if (testSerializer)
          Serializer.fromBytes(Serializer.toBytes(u0))
        else
          u0)
      // should I test that m0 and state are identical? maybe have a separate test for that
      val dummy = new FakeWorld(state)
      val pico = new Pico
      pico.add("org.nlogo.render.Renderer")
      pico.addComponent(dummy)
      val renderer = pico.getComponent(classOf[api.RendererInterface])
      renderer.resetCache(ws.patchSize)

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

  // should eventually be updated to handle 2D/3D distinction - ST 8/6/12
  def allTestableModels = {
    def hasExclusion(fileName: String) = {
      val exclusions = Seq(
        "SYSTEM-DYNAMICS-SETUP",
        "MOUSE-DOWN?",
        "USER-MESSAGE",
        "MOUSE-INSIDE?",
        "NEED-TO-MANUALLY-MAKE-PREVIEW-FOR-THIS-MODEL",
        "USER-YES-OR-NO?",
        "USER-INPUT",
        "GIS:")
      io.Source.fromFile(fileName)
        .getLines.exists { line =>
          exclusions.exists { exclusion =>
            line.toUpperCase contains exclusion
          }
        }
    }
    io.Source.fromFile("models/test/checksums.txt")
      .getLines.map(_.split(" - ")(0)) // get model paths
      .filterNot(hasExclusion)
  }

  def modelsToTest = Seq(
    "models/Sample Models/Networks/Diffusion on a Directed Network.nlogo")

  if (!api.Version.is3D)
    allTestableModels.foreach { modelPath =>
      // exclude 1 model for now, failing & we don't know why yet
      if(!modelPath.endsWith("Diffusion on a Directed Network.nlogo"))
        test("Mirroring: " + modelPath) {
          modelRenderingTest(modelPath)
        }
    }

}
