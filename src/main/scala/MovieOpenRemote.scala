package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Command, Context, ExtensionException }
import org.nlogo.core.Syntax

class MovieOpenRemote(vid: VideoSourceContainer, files: MovieFactory) extends Command {
  override def getSyntax = Syntax.commandSyntax(List[Int](Syntax.StringType))

  override def perform(args: Array[Argument], context: Context): Unit = {
    try {
      val uri = args(0).getString
      vid.videoSource = files.openRemote(uri)
      if (vid.videoSource.isEmpty)
        throw new ExtensionException("vid: no movie found")
    } catch {
      case e: InvalidFormatException =>
        throw new ExtensionException("vid: format not supported")
      case e: InvalidProtocolException =>
        throw new ExtensionException("vid: protocol not supported")
    }
  }
}
