// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import AgentVariableNumbers._

object AgentVariables {

  def getImplicitObserverVariables =
    Array[String]()

  def getImplicitTurtleVariables(is3D: Boolean) =
    if (is3D)
      Array("WHO", "COLOR", "HEADING", "PITCH", "ROLL", "XCOR", "YCOR", "ZCOR", "SHAPE",
            "LABEL", "LABEL-COLOR", "BREED", "HIDDEN?", "SIZE", "PEN-SIZE", "PEN-MODE")
    else
      Array("WHO", "COLOR", "HEADING", "XCOR", "YCOR", "SHAPE", "LABEL", "LABEL-COLOR", "BREED",
            "HIDDEN?", "SIZE", "PEN-SIZE", "PEN-MODE")

  def getImplicitPatchVariables(is3D: Boolean) =
    if (is3D)
      Array("PXCOR", "PYCOR", "PZCOR", "PCOLOR", "PLABEL", "PLABEL-COLOR")
    else
      Array("PXCOR", "PYCOR", "PCOLOR", "PLABEL", "PLABEL-COLOR")

  def getImplicitLinkVariables =
    Array("END1", "END2", "COLOR", "LABEL", "LABEL-COLOR", "HIDDEN?", "BREED",
          "THICKNESS", "SHAPE", "TIE-MODE")

  private val doubleTurtleVariables2D = Set(
    VAR_WHO, VAR_HEADING, VAR_XCOR, VAR_YCOR, VAR_SIZE, VAR_PENSIZE)
  private val doubleTurtleVariables3D = Set(
    VAR_HEADING3D, VAR_PITCH3D, VAR_ROLL3D, VAR_XCOR3D, VAR_YCOR3D, VAR_ZCOR3D, VAR_SIZE3D, VAR_PENSIZE3D)

  def isDoubleTurtleVariable(vn: Int, is3D: Boolean): Boolean =
    if (is3D)
      doubleTurtleVariables3D(vn)
    else
      doubleTurtleVariables2D(vn)

  def isSpecialTurtleVariable(vn: Int) =
    vn == VAR_WHO

  def isDoublePatchVariable(vn: Int, is3D: Boolean) =
    if (is3D)
      vn == VAR_PXCOR3D || vn == VAR_PYCOR3D || vn == VAR_PZCOR3D
    else
      vn == VAR_PXCOR || vn == VAR_PYCOR

  def isSpecialPatchVariable(vn: Int, is3D: Boolean) =
    if (is3D)
      vn == VAR_PXCOR3D || vn == VAR_PYCOR3D || vn == VAR_PZCOR3D
    else
      vn == VAR_PXCOR || vn == VAR_PYCOR

  def isDoubleLinkVariable(vn: Int) =
    vn == VAR_THICKNESS

  def isSpecialLinkVariable(vn: Int) =
    vn == VAR_END1 || vn == VAR_END2 || vn == VAR_LBREED

}
