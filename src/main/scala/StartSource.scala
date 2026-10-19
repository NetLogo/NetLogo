package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Command, Context, ExtensionException }
import org.nlogo.core.Syntax

class StartSource(vid: VideoSourceContainer) extends Command {
  def getSyntax = Syntax.commandSyntax(List())

  def perform(args: Array[Argument], context: Context): Unit = {
    if (vid.videoSource.isEmpty)
      throw new ExtensionException("vid: no selected source")
    vid.videoSource.foreach(_.play())
  }
}
