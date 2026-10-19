package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Context, Command, ExtensionException }
import org.nlogo.core.Syntax

class StartRecorder(recorder: Recorder) extends Command {

  def getSyntax = Syntax.commandSyntax(
    right = List(Syntax.NumberType | Syntax.RepeatableType),
    defaultOption = Some(0))

  def perform(args: Array[Argument], context: Context): Unit = {
    try {
      recorder.start()
      if (args.length >= 2) {
        val width = args(0).getIntValue
        val height = args(1).getIntValue
        if (width <= 0 || height <= 0)
          throw new ExtensionException("vid: invalid dimensions")
        recorder.setResolution(args(0).getIntValue, args(1).getIntValue)
      }
    } catch {
      case Recorder.AlreadyStarted => throw new ExtensionException("vid: recorder already started")
    }
  }
}
