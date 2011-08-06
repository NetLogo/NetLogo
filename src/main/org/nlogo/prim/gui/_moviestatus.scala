package org.nlogo.prim.gui

import org.nlogo.api.LogoException
import org.nlogo.awt.MovieEncoder
import org.nlogo.nvm.{ Context, EngineException, Reporter, Syntax }
import org.nlogo.window.GUIWorkspace

class _moviestatus extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_STRING)
  override def report(context: Context) = {
    workspace match {
      case gw: GUIWorkspace =>
        status(gw.movieEncoder)
      case _ =>
        throw new EngineException(
          context, this, token.name + " can only be used in the GUI")
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
