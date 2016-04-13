// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Model

import scala.util.Try

trait ComponentSerialization[A, B <: ModelFormat[A, _]] {
  def componentName: String
  def addDefault: Model => Model = identity
  def serialize(m: Model): A
  def validationErrors(m: Model): Option[String]
  def deserialize(s: A): Model => Model = identity
}

trait ModelFormat[Section, Format <: ModelFormat[Section, _]] {
  def name: String

  def sections(location: java.net.URI): Try[Map[String, Section]]

  def baseModel: Model = Model()

  def version:             ComponentSerialization[Section, Format]
  def codeComponent:       ComponentSerialization[Section, Format]
  def infoComponent:       ComponentSerialization[Section, Format]
  def interfaceComponent:  ComponentSerialization[Section, Format]
  def shapesComponent:     ComponentSerialization[Section, Format]
  def linkShapesComponent: ComponentSerialization[Section, Format]

  def defaultComponents =
    Seq(version, codeComponent, infoComponent, interfaceComponent, shapesComponent, linkShapesComponent)

  def load(location: java.net.URI, optionalComponents: Seq[ComponentSerialization[Section, Format]]): Try[Model] = {
    for {
      loadedSections <- sections(location)
    } yield (defaultComponents ++ optionalComponents).foldLeft(baseModel) {
      case (model, component) =>
        val addComponent = loadedSections
          .get(component.componentName)
          .map(component.deserialize _)
          .getOrElse(component.addDefault)
        addComponent(model)
    }
  }
}

case class ModelSettings(snapToGrid: Boolean)
