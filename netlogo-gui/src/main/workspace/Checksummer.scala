// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.{ PreviewCommands, Workspace }
import org.nlogo.util.HexString.toHexString
import java.io.PrintWriter

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

  def calculateWorldChecksum(workspace: Workspace): String =
    calculateChecksum(workspace.exportWorld, stripMetaSection)

  def calculateGraphicsChecksum(workspace: Workspace): String =
    calculateChecksum{writer =>
      val raster = workspace.renderer.exportView(workspace)
      raster.getData.getPixels(0, 0, raster.getWidth, raster.getHeight, null: Array[Int])
        .foreach(writer.println)
    }

  // public for testing - ST 7/15/10
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
    toHexString(digester.digest(output.getBytes))
  }

  private def stripMetaSection(exportStr: String): String =
    exportStr.drop(exportStr.indexOf("\n\n") + 2)

}
