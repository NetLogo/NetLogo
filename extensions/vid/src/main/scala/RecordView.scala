package org.nlogo.extensions.vid

import java.awt.image.BufferedImage

import org.nlogo.api.{ Argument, Context, Command, ExtensionException, ReporterRunnable }
import org.nlogo.core.Syntax
import org.nlogo.window.GUIWorkspace

class RecordView(recorder: Recorder) extends Command {

  def getSyntax = Syntax.commandSyntax(right = List())

  def perform(args: Array[Argument], context: Context): Unit = {
    if (! recorder.isRecording)
      throw new ExtensionException("vid: recorder not started")
    context.workspace match {
      case gw: GUIWorkspace =>
        val exportedView = gw.waitForResult(new ReporterRunnable[BufferedImage] {
          override def run(): BufferedImage = gw.exportView
        })
        recorder.recordFrame(exportedView)
      case _ => recorder.recordFrame(context.workspace.exportView)
    }
  }
}
