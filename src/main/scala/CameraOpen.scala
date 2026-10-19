package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Command, Context, ExtensionException }
import org.nlogo.core.Syntax

class CameraOpen(vid: VideoSourceContainer, cameras: CameraFactory) extends Command {
  override def getSyntax =
    Syntax.commandSyntax(right = List(Syntax.StringType | Syntax.RepeatableType), defaultOption = Some(0))

  def perform(args: Array[Argument], context: Context): Unit = {
    val cameraName =
      if (args.length == 0)
        cameras.defaultCameraName.getOrElse(
          throw new ExtensionException("vid: no cameras found"))
      else
        args(0).getString
    vid.closeSource()
    cameras.open(cameraName) match {
      case cam@Some(camera) => vid.videoSource = Some(camera)
      case _                =>
        throw new ExtensionException(s"""vid: camera "$cameraName" not found""")
    }
  }
}
