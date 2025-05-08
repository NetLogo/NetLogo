// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.PrintWriter
import org.nlogo.api.{ PreviewCommands, Workspace }
import org.nlogo.util.HexString

object Checksummer {

  def initModelForChecksumming(workspace: Workspace, variant: String): Unit = {
    workspace.renderer.setRenderLabelsAsRectangles(true)
    val commands = workspace.previewCommands match {
      case PreviewCommands.Custom(source) => source
      case _ => PreviewCommands.Default.source // may or may not compile, but we'll try
    }
    workspace.command(s"random-seed 0\n$commands\n$variant")
  }

  def calculateWorldChecksum(workspace: Workspace): String =
    calculateChecksum(workspace.exportWorld _, stripMetaSection)

  def calculateGraphicsChecksum(workspace: Workspace): String =
    calculateChecksum{writer =>
      val raster = workspace.renderer.exportView(workspace)
      raster.getData.getPixels(0, 0, raster.getWidth, raster.getHeight, null: Array[Int])
        .foreach(writer.println)
    }

  def calculateChecksum(fn: PrintWriter => Unit, transformer: (String) => String = identity): String = {
    val output = {
      val outputStream = new java.io.ByteArrayOutputStream
      val writer = new java.io.PrintWriter(outputStream)
      fn(writer)
      writer.close()
      outputStream.close()
      val cleansed = outputStream.toString.replaceAll("\r\n", "\n")  // avoid platform differences
      transformer(cleansed)
    }
    val digester = java.security.MessageDigest.getInstance("SHA")
    HexString.toHexString(digester.digest(output.getBytes))
  }

  private def stripMetaSection(exportStr: String): String =
    exportStr.drop(exportStr.indexOf("\n\n") + 2)

}
