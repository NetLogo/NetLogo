package org.nlogo.extensions.vid

import org.nlogo.core.Syntax
import org.nlogo.api.{ Argument, Command, Context }

class StopSource(vid: VideoSourceContainer) extends Command {
  def getSyntax = Syntax.commandSyntax(List())

  def perform(args: Array[Argument], context: Context): Unit = {
    vid.videoSource.foreach(_.stop())
  }
}
