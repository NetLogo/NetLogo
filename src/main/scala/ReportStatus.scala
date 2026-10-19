package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Context, Reporter }
import org.nlogo.core.Syntax

class ReportStatus(vid: VideoSourceContainer) extends Reporter {

  def getSyntax = Syntax.reporterSyntax(right = List(), ret = Syntax.StringType)

  def report(args: Array[Argument], context: Context): AnyRef = {
    vid.videoSource match {
      case Some(source) if source.isPlaying => "playing"
      case Some(source)                     => "stopped"
      case None                             => "inactive"
    }
  }
}
