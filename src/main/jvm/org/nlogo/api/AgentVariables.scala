// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object AgentVariables {

  val getImplicitObserverVariables =
    Seq[String]()

  val getImplicitTurtleVariables =
    Seq("WHO", "COLOR", "HEADING", "XCOR", "YCOR", "SHAPE", "LABEL", "LABEL-COLOR", "BREED",
        "HIDDEN?", "SIZE", "PEN-SIZE", "PEN-MODE")

  val getImplicitPatchVariables =
    Seq("PXCOR", "PYCOR", "PCOLOR", "PLABEL", "PLABEL-COLOR")

  val getImplicitLinkVariables =
    Seq("END1", "END2", "COLOR", "LABEL", "LABEL-COLOR", "HIDDEN?", "BREED",
        "THICKNESS", "SHAPE", "TIE-MODE")

}
