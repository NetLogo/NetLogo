// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Syntax

import AgentVariableNumbers._

import scala.collection.immutable.ListMap

object AgentVariables {

  private val colorType = Syntax.NumberType | Syntax.ListType

  def implicitObserverVariableTypeMap =
    ListMap[String, Int]()

  def implicitTurtleVariableTypeMap(is3D: Boolean) =
    if (is3D)
      ListMap("WHO"   -> Syntax.NumberType,
        "COLOR"       -> colorType,
        "HEADING"     -> Syntax.NumberType,
        "PITCH"       -> Syntax.NumberType,
        "ROLL"        -> Syntax.NumberType,
        "XCOR"        -> Syntax.NumberType,
        "YCOR"        -> Syntax.NumberType,
        "ZCOR"        -> Syntax.NumberType,
        "SHAPE"       -> Syntax.StringType,
        "LABEL"       -> Syntax.WildcardType,
        "LABEL-COLOR" -> colorType,
        "BREED"       -> Syntax.AgentsetType,
        "HIDDEN?"     -> Syntax.BooleanType,
        "SIZE"        -> Syntax.NumberType,
        "PEN-SIZE"    -> Syntax.NumberType,
        "PEN-MODE"    -> Syntax.StringType)
    else
      ListMap("WHO"   -> Syntax.NumberType,
        "COLOR"       -> colorType,
        "HEADING"     -> Syntax.NumberType,
        "XCOR"        -> Syntax.NumberType,
        "YCOR"        -> Syntax.NumberType,
        "SHAPE"       -> Syntax.StringType,
        "LABEL"       -> Syntax.WildcardType,
        "LABEL-COLOR" -> colorType,
        "BREED"       -> Syntax.AgentsetType,
        "HIDDEN?"     -> Syntax.BooleanType,
        "SIZE"        -> Syntax.NumberType,
        "PEN-SIZE"    -> Syntax.NumberType,
        "PEN-MODE"    -> Syntax.StringType)

  def implicitPatchVariableTypeMap(is3D: Boolean) =
    if (is3D)
      ListMap("PXCOR"  -> Syntax.NumberType,
        "PYCOR"        -> Syntax.NumberType,
        "PZCOR"        -> Syntax.NumberType,
        "PCOLOR"       -> colorType,
        "PLABEL"       -> Syntax.WildcardType,
        "PLABEL-COLOR" -> colorType)
    else
      ListMap("PXCOR"  -> Syntax.NumberType,
        "PYCOR"        -> Syntax.NumberType,
        "PCOLOR"       -> colorType,
        "PLABEL"       -> Syntax.WildcardType,
        "PLABEL-COLOR" -> colorType)

  def implicitLinkVariableTypeMap =
    ListMap("END1"  -> Syntax.TurtleType,
      "END2"        -> Syntax.TurtleType,
      "COLOR"       -> colorType,
      "LABEL"       -> Syntax.WildcardType,
      "LABEL-COLOR" -> colorType,
      "HIDDEN?"     -> Syntax.BooleanType,
      "BREED"       -> Syntax.LinksetType,
      "THICKNESS"   -> Syntax.NumberType,
      "SHAPE"       -> Syntax.StringType,
      "TIE-MODE"    -> Syntax.StringType)

  val implicitObserverVariables              = implicitObserverVariableTypeMap.keys.toSeq
  def implicitTurtleVariables(is3D: Boolean) = implicitTurtleVariableTypeMap(is3D).keys.toSeq
  def implicitPatchVariables(is3D: Boolean)  = implicitPatchVariableTypeMap(is3D).keys.toSeq
  val implicitLinkVariables                  = implicitLinkVariableTypeMap.keys.toSeq

  val getImplicitObserverVariables              = implicitObserverVariableTypeMap.keys.toSeq.toArray
  def getImplicitTurtleVariables(is3D: Boolean) = implicitTurtleVariableTypeMap(is3D).keys.toSeq.toArray
  def getImplicitPatchVariables(is3D: Boolean)  = implicitPatchVariableTypeMap(is3D).keys.toSeq.toArray
  val getImplicitLinkVariables                  = implicitLinkVariableTypeMap.keys.toSeq.toArray

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
