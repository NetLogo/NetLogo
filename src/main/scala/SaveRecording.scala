package org.nlogo.extensions.vid

import java.io.FileNotFoundException
import java.nio.file.Paths

import org.nlogo.api.{ Argument, Context, Command, ExtensionException }
import org.nlogo.core.Syntax

class SaveRecording(recorder: Recorder) extends Command {

  def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType))

  def perform(args: Array[Argument], context: Context): Unit = {
    try {
      val pathString = args(0).getString.stripSuffix(".mp4") + ".mp4"
      val path = Paths.get(pathString)
      val savePath =
        if (path.isAbsolute) path
        else Paths.get(context.attachCurrentDirectory(pathString))
      recorder.save(savePath)
    } catch {
      case _: FileNotFoundException => throw new ExtensionException("vid: no such directory")
      case Recorder.NotRecording    => throw new ExtensionException("vid: recorder not started")
      case Recorder.NoFrames        => throw new ExtensionException("vid: no frames recorded")
    }
  }
}

