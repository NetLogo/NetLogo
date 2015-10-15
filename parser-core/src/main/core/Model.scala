// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import Shape.{ VectorShape, LinkShape }

import ShapeParser.{ parseVectorShapes, parseLinkShapes }

case class Model(code: String = "", widgets: List[Widget] = List(View()), info: String = "", version: String = "NetLogo 5.0",
  turtleShapes: List[VectorShape] = Model.defaultShapes, behaviorSpace: List[String] = Nil,
  linkShapes: List[LinkShape] = Model.defaultLinkShapes, previewCommands: List[String] = Nil) {

  def interfaceGlobals: List[String] = widgets.collect{case x:DeclaresGlobal => x}.map(_.varName)
  def constraints: Map[String, ConstraintSpecification] = widgets.collect{case x:DeclaresConstraint => (x.varName, x.constraint)}.toMap
  def interfaceGlobalCommands: List[String] = widgets.collect{case x:DeclaresGlobalCommand => x}.map(_.command)

  if(widgets.collectFirst{case (w: View) => w}.isEmpty)
    throw new RuntimeException(
      "Every model must have at least a view...")

  def view: View = widgets.collectFirst{case (w: View) => w}.get
  def plots: List[Plot] = widgets.collect{case (w: Plot) => w}
}

object Model {
  lazy val defaultShapes: List[VectorShape] =
    parseVectorShapes(Resource.lines("/system/defaultShapes.txt").toSeq).toList
  lazy val defaultLinkShapes: List[LinkShape] =
    parseLinkShapes(Resource.lines("/system/defaultLinkShapes.txt").toSeq).toList
}
