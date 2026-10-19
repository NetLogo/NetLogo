package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Command, Context, ExtensionException }
import org.nlogo.core.Syntax

class CameraSelect(videoContainer: VideoSourceContainer, cameras: CameraFactory, selector: Selector) extends Command {
  override def getSyntax = Syntax.commandSyntax(List[Int]())

  override def perform(args: Array[Argument], context: Context): Unit = {
    if (cameras.cameraNames.isEmpty)
      throw new ExtensionException("vid: no cameras found")
    videoContainer.closeSource()
    selector.selectOneOf(cameras.cameraNames).foreach { cameraName =>
      videoContainer.videoSource = cameras.open(cameraName)
    }
  }
}
