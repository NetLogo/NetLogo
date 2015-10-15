// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.Syntax
import org.nlogo.awt.MovieEncoder
import org.nlogo.nvm.{ Context, EngineException, Reporter }
import org.nlogo.window.GUIWorkspace

class _moviestatus extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType)
  override def report(context: Context) = {
    workspace match {
      case gw: GUIWorkspace =>
        status(gw.movieEncoder)
      case _ =>
        throw new EngineException(
          context, this, token.text + " can only be used in the GUI")
    }
  }
  private def status(encoder: MovieEncoder) =
    if(encoder == null)
      "No movie."
    else {
      val builder = new StringBuilder
      builder ++= encoder.getNumFrames + " frames" + "; "
      builder ++= "frame rate = " + encoder.getFrameRate
      if (encoder.isSetup) {
        val size = encoder.getFrameSize
        builder ++= "; size = " + size.width + "x" + size.height
      }
      builder.toString
    }
}
