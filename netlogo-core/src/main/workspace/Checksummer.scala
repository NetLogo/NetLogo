// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.awt.image.BufferedImage
import java.io.{ ByteArrayOutputStream, PrintWriter }
import java.security.MessageDigest
import javax.imageio.ImageIO

import org.nlogo.api.{ PreviewCommands, Workspace }
import org.nlogo.util.HexString

object Checksummer {
  def initModelForChecksumming(workspace: Workspace, variant: String): Unit = {
    workspace.renderer.setRenderLabelsAsRectangles(true)
    val source = workspace.previewCommands match {
      case PreviewCommands.Custom(source) => source
      case _ => PreviewCommands.Default.source // may or may not compile, but we'll try
    }
    workspace.seedRNGs(0)
    workspace.command(s"$source\n$variant")
  }

  def exportWorld(workspace: Workspace): String = {
    val stream = new ByteArrayOutputStream
    val writer = new PrintWriter(stream)

    workspace.exportWorld(writer)

    writer.close()
    stream.close()

    cleanExportString(stream.toString)
  }

  def calculateWorldChecksum(workspace: Workspace): String =
    calculateChecksum(workspace.exportWorld, cleanExportString)

  def exportGraphics(workspace: Workspace): Array[Byte] = {
    val stream = new ByteArrayOutputStream

    ImageIO.write(workspace.renderer.exportView(workspace), "png", stream)

    stream.close()

    stream.toByteArray
  }

  def calculateGraphicsChecksum(workspace: Workspace): String =
    calculateGraphicsChecksum(workspace.renderer.exportView(workspace))

  def calculateGraphicsChecksum(image: BufferedImage): String = {
    calculateChecksum { writer =>
      image.getData.getPixels(0, 0, image.getWidth, image.getHeight, null: Array[Int]).foreach(writer.println)
    }
  }

  // public for testing - ST 7/15/10
  def calculateChecksum(fn: PrintWriter => Unit, transformer: String => String = identity): String = {
    val output: String = {
      val outputStream = new ByteArrayOutputStream
      val writer = new PrintWriter(outputStream)

      fn(writer)

      writer.close()
      outputStream.close()

      transformer(outputStream.toString)
    }

    HexString.toHexString(MessageDigest.getInstance("SHA").digest(output.getBytes))
  }

  private def cleanExportString(exportStr: String): String = {
    val norm = exportStr.replaceAll("\r\n", "\n")

    norm.drop(norm.indexOf("\n\n") + 2)
  }
}
