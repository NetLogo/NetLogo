// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import Shape.{ VectorShape, LinkShape }

import ShapeParser.{ parseVectorShapes, parseLinkShapes }
import scala.reflect.ClassTag

case class Model(code: String = "",
  widgets: Seq[Widget] = List(View()),
  info: String = "",
  version: String = "NetLogo 6.1",
  turtleShapes: Seq[VectorShape] = Model.defaultShapes,
  linkShapes: Seq[LinkShape] = Model.defaultLinkShapes,
  optionalSections: Seq[OptionalSection[_]] = Seq()) {

  def interfaceGlobals: Seq[String] = widgets.collect{case x:DeclaresGlobal => x}.map(_.varName)
  def constraints: Map[String, ConstraintSpecification] = widgets.collect{case x:DeclaresConstraint => (x.varName, x.constraint)}.toMap
  def interfaceGlobalCommands: Seq[String] = widgets.collect{case x:DeclaresGlobalCommand => x}.map(_.command)

  if(widgets.collectFirst{case (w: View) => w}.isEmpty)
    throw new Model.InvalidModelError("Every model must have at least a view...")

  def view: View = widgets.collectFirst{case (w: View) => w}.get
  def plots: Seq[Plot] = widgets.collect{case (w: Plot) => w}

  def modelInfo =
    optionalSectionValue[ModelInfo](ModelInfo.sectionKey).getOrElse(ModelInfo.empty)

  /* true only when the value is not a default */
  def hasValueForOptionalSection(key: String): Boolean = {
    optionalSections.find(_.key == key).isDefined
  }

  def optionalSectionValue[A <: AnyRef](key: String)(implicit ct: ClassTag[OptionalSection[A]]): Option[A] = {
    optionalSections.find(_.key == key)
      .flatMap(ct.unapply)
      .map(sect => sect.get.getOrElse(sect.default))
  }

  def withOptionalSection[A <: AnyRef](key: String, sectionValue: Option[A], default: A): Model =
    copy(optionalSections =
      optionalSections.filterNot(_.key == key) :+
      new OptionalSection[A](key, sectionValue, default))
}

object Model {
  lazy val defaultShapes: List[VectorShape] =
    parseVectorShapes(Resource.lines("/system/defaultShapes.txt").toSeq).toList
  lazy val defaultLinkShapes: List[LinkShape] =
    parseLinkShapes(Resource.lines("/system/defaultLinkShapes.txt").toSeq).toList
  class InvalidModelError(message: String) extends RuntimeException(message)
}

class OptionalSection[A <: AnyRef](val key: String, value: Option[A], val default: A) {
  def get: Option[A] = value
}

