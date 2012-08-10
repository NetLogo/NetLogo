// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import AgentVariableNumbers._

object AgentVariables {

  val getImplicitObserverVariables = Seq[String]()

  val getImplicitTurtleVariables =
    Seq("WHO", "COLOR", "HEADING", "XCOR", "YCOR", "SHAPE", "LABEL", "LABEL-COLOR", "BREED",
        "HIDDEN?", "SIZE", "PEN-SIZE", "PEN-MODE")

  val getImplicitPatchVariables =
    Seq("PXCOR", "PYCOR", "PCOLOR", "PLABEL", "PLABEL-COLOR")

  val getImplicitLinkVariables =
    Seq("END1", "END2", "COLOR", "LABEL", "LABEL-COLOR", "HIDDEN?", "BREED",
        "THICKNESS", "SHAPE", "TIE-MODE")

  private val doubleTurtleVariables = Set(
    VAR_WHO, VAR_HEADING, VAR_XCOR, VAR_YCOR, VAR_SIZE, VAR_PENSIZE)

  def isDoubleTurtleVariable(vn: Int): Boolean =
    doubleTurtleVariables(vn)

  def isSpecialTurtleVariable(vn: Int) =
    vn == VAR_WHO

  def isDoublePatchVariable(vn: Int) =
    vn == VAR_PXCOR || vn == VAR_PYCOR

  def isSpecialPatchVariable(vn: Int) =
    vn == VAR_PXCOR || vn == VAR_PYCOR

  def isDoubleLinkVariable(vn: Int) =
    vn == VAR_THICKNESS

  def isSpecialLinkVariable(vn: Int) =
    vn == VAR_END1 || vn == VAR_END2 || vn == VAR_LBREED

}
