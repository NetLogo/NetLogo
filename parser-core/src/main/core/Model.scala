// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import Shape.{ VectorShape, LinkShape }

import ShapeParser.{ parseVectorShapes, parseLinkShapes }
import scala.reflect.ClassTag

case class Model(code: String = "",
  widgets: Seq[Widget] = List(View()),
  info: String = "",
  version: String = "NetLogo 7.0.0-beta2",
  turtleShapes: Seq[VectorShape] = Model.defaultTurtleShapes,
  linkShapes: Seq[LinkShape] = Model.defaultLinkShapes,
  optionalSections: Seq[OptionalSection[?]] = Seq(),
  resources: Seq[ExternalResource] = Seq(),
  // these are sections in the XML format that were not recognized by the current version but should
  // remain in the model file just in case for the user's benefit (Isaac B 7/6/25)
  unknownSections: Seq[XMLElement] = Seq()) {

  def interfaceGlobals: Seq[String] = widgets.collect{case x:DeclaresGlobal => x}.map(_.varName)
  def constraints: Map[String, ConstraintSpecification] = widgets.collect{case x:DeclaresConstraint => (x.varName, x.constraint)}.toMap
  def interfaceGlobalCommands: Seq[String] = widgets.collect{case x:DeclaresGlobalCommand => x}.map(_.command)

  if(widgets.collectFirst{case (w: ViewLike) => w}.isEmpty)
    throw new Model.InvalidModelError("Every model must have at least a view...")

  def view: View = widgets.collectFirst{case (w: View) => w}.get
  def plots: Seq[Plot] = widgets.collect{case (w: Plot) => w}

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
  val defaultCode = ""
  lazy val defaultTurtleShapes: Seq[VectorShape] =
    parseVectorShapes(Resource.lines("/system/defaultTurtleShapes.txt").toSeq)
  lazy val libraryTurtleShapes: Seq[VectorShape] =
    parseVectorShapes(Resource.lines("/system/libraryTurtleShapes.txt").toSeq)
  lazy val defaultLinkShapes: Seq[LinkShape] =
    parseLinkShapes(Resource.lines("/system/defaultLinkShapes.txt").toSeq)
  lazy val defaultView =
    View(368, 10, 434, 434, WorldDimensions(-16, 16, -16, 16, 13.0), 10, UpdateMode.Continuous, true, None, 30)
  class InvalidModelError(message: String) extends RuntimeException(message)
}

class OptionalSection[A <: AnyRef](val key: String, value: Option[A], val default: A) {
  def get: Option[A] = value
}

class Section[A <: AnyRef](key: String, value: A) extends OptionalSection[A](key, Some(value), value)

case class ModelSettings(snapToGrid: Boolean)
