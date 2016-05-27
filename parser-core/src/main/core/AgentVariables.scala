// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.collection.immutable.ListMap

object AgentVariables extends AgentVariableSet {

  val implicitObserverVariableTypeMap =
    ListMap[String, Int]()

  private val colorType = Syntax.NumberType | Syntax.ListType

  val implicitTurtleVariableTypeMap =
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

  val implicitPatchVariableTypeMap =
    ListMap("PXCOR"  -> Syntax.NumberType,
      "PYCOR"        -> Syntax.NumberType,
      "PCOLOR"       -> colorType,
      "PLABEL"       -> Syntax.WildcardType,
      "PLABEL-COLOR" -> colorType)

  val implicitLinkVariableTypeMap =
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

  val implicitObserverVariables = implicitObserverVariableTypeMap.keys.toSeq
  val implicitTurtleVariables = implicitTurtleVariableTypeMap.keys.toSeq
  val implicitPatchVariables = implicitPatchVariableTypeMap.keys.toSeq
  val implicitLinkVariables = implicitLinkVariableTypeMap.keys.toSeq

  val getImplicitObserverVariables = implicitObserverVariableTypeMap.keys.toSeq.toArray
  def getImplicitTurtleVariables   = implicitTurtleVariableTypeMap.keys.toSeq.toArray
  def getImplicitPatchVariables    = implicitPatchVariableTypeMap.keys.toSeq.toArray
  val getImplicitLinkVariables     = implicitLinkVariableTypeMap.keys.toSeq.toArray
}
