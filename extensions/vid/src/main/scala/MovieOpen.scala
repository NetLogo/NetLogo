package org.nlogo.extensions.vid

import java.io.File

import org.nlogo.api.{ Argument, Command, Context, ExtensionException }
import org.nlogo.core.Syntax

class MovieOpen(vid: VideoSourceContainer, files: MovieFactory) extends Command {
  override def getSyntax = Syntax.commandSyntax(right = List[Int](Syntax.StringType))

  def perform(args: Array[Argument], context: Context): Unit = {
    val providedPath = args(0).getString
    val file = new File(providedPath)
    val filePath =
      if (file.isAbsolute)
        providedPath
      else
        context.attachCurrentDirectory(providedPath)
    try {
      vid.videoSource = files.open(filePath)
      if (vid.videoSource.isEmpty)
        throw new ExtensionException("vid: no movie found")
    } catch {
      case e: InvalidFormatException =>
        throw new ExtensionException("vid: format not supported")
    }
  }
}
