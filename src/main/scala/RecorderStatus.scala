package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Context, Reporter }
import org.nlogo.core.Syntax

class RecorderStatus(recorder: Recorder) extends Reporter {

  def getSyntax = Syntax.reporterSyntax(right = List(), ret = Syntax.StringType)

  def report(args: Array[Argument], context: Context): AnyRef = {
    if (recorder.isRecording) "recording"
    else "inactive"
  }
}
