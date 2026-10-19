package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Context, Command, ExtensionException }
import org.nlogo.core.Syntax

class MovieSelect(videoSourceContainer: VideoSourceContainer, movies: MovieFactory, selector: Selector) extends Command {
  override def getSyntax = Syntax.commandSyntax()

  override def perform(args: Array[Argument], context: Context): Unit = {
    selector.selectFile.foreach { path =>
      try {
        videoSourceContainer.videoSource = movies.open(path)
      } catch {
        case e: InvalidFormatException =>
          throw new ExtensionException("vid: format not supported")
      }
    }
  }
}
