package org.nlogo.extensions.vid

import java.nio.file.Files
import javax.imageio.ImageIO

import org.nlogo.core.Syntax
import org.nlogo.api.{ Argument, Context, Command, ExtensionException }

class RecordInterface(recorder: Recorder) extends Command {

  def getSyntax = Syntax.commandSyntax(right = List())

  def perform(args: Array[Argument], context: Context): Unit = {
    if (! recorder.isRecording)
      throw new ExtensionException("vid: recorder not started")
    try {
      val tmpFile = Files.createTempFile("interfaceexport", "png")
      context.workspace.exportInterface(tmpFile.toString)
      val exportedImage = ImageIO.read(tmpFile.toFile)
      recorder.recordFrame(exportedImage)
    } catch {
      case _: UnsupportedOperationException => throw new ExtensionException("vid: export interface not supported")
    }
  }
}
