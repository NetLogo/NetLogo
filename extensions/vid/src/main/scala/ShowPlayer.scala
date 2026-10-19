package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Command, Context, ExtensionException }
import org.nlogo.core.Syntax

class ShowPlayer(player: Player, vidExtension: VideoSourceContainer) extends Command {
  override def getSyntax =
    Syntax.commandSyntax(right = List[Int](Syntax.NumberType | Syntax.RepeatableType), defaultOption = Some(0))

  override def perform(args: Array[Argument], context: Context): Unit = {

    lazy val width  = args(0).getDoubleValue
    lazy val height = args(1).getDoubleValue

    if (args.length != 0 && args.length != 2)
      throw new ExtensionException("show-player expected 2 arguments")
    else if (args.length == 2 && (width <= 0 || height <= 0))
      throw new ExtensionException("vid: invalid dimensions")

    val boundedNode =
      (args.length, vidExtension.videoSource) match {
        case (2, Some(videoSource)) =>
          videoSource.videoNode(Some((width, height)))
        case (_, Some(videoSource)) =>
          videoSource.videoNode(None)
        case (2, None) =>
          player.emptyNode(Some((width, height)))
        case _ =>
          player.emptyNode(None)
      }
    player.present(boundedNode)
    player.show()
  }
}
