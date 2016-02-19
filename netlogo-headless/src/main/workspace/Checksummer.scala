// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.PrintWriter
import org.nlogo.api.{ PreviewCommands, Workspace }
import org.nlogo.api.HexString

object Checksummer {
  def initModelForChecksumming(workspace: Workspace) {
    workspace.renderer.renderLabelsAsRectangles_=(true)
    val commands = workspace.previewCommands match {
      case PreviewCommands.Custom(source) => source
      case _ => PreviewCommands.Default.source // may or may not compile, but we'll try
    }
    workspace.command("random-seed 0\n" + commands)
  }
  def calculateWorldChecksum(workspace: Workspace): String =
    calculateChecksum(workspace.exportWorld _)
  def calculateGraphicsChecksum(workspace: Workspace): String =
    calculateChecksum{writer =>
      val raster = workspace.renderer.exportView(workspace)
      raster.getData.getPixels(0, 0, raster.getWidth, raster.getHeight, null: Array[Int])
        .foreach(writer.println)
    }
  def calculateChecksum(fn: PrintWriter => Unit): String = {
    val output = {
      val outputStream = new java.io.ByteArrayOutputStream
      val writer = new java.io.PrintWriter(outputStream)
      fn(writer)
      writer.close()
      outputStream.close()
      outputStream.toString.replaceAll("\r\n", "\n")  // avoid platform differences
    }
    val digester = java.security.MessageDigest.getInstance("SHA")
    HexString.toHexString(digester.digest(output.getBytes))
  }
}
