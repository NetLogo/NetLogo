// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.workspace.AbstractWorkspaceScala
import org.nlogo.util.HexString.toHexString
import java.io.PrintWriter

object Checksummer {
  def initModelForChecksumming(workspace: HeadlessWorkspace) {
    if(workspace.previewCommands.containsSlice("need-to-manually-make-preview-for-this-model"))
      workspace.previewCommands = AbstractWorkspaceScala.DefaultPreviewCommands
    workspace.command("random-seed 0")
    workspace.command(workspace.previewCommands)
  }
  def calculateWorldChecksum(workspace: HeadlessWorkspace): String =
    calculateChecksum(workspace.exportWorld _)
  def calculateGraphicsChecksum(workspace: HeadlessWorkspace): String =
    calculateChecksum(workspace.writeGraphicsData _)
  // public for testing - ST 7/15/10
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
