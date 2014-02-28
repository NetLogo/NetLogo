// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.util.HexString.toHexString
import java.io.PrintWriter
import org.nlogo.api.Workspace

object Checksummer {
  def initModelForChecksumming(workspace: Workspace) {
    workspace.renderer.renderLabelsAsRectangles_=(true)
    val commands =
      Some(workspace.previewCommands)
        .filterNot(_.containsSlice("need-to-manually-make-preview-for-this-model"))
        .getOrElse(AbstractWorkspace.DefaultPreviewCommands)
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
    toHexString(digester.digest(output.getBytes))
  }
}
