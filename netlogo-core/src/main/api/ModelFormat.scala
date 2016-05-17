// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.net.URI

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

  def sections(location: URI): Try[Map[String, Section]]
  def writeSections(sections: Map[String, Section], location: URI): Try[URI]
  def sectionsToSource(sections: Map[String, Section]): Try[String]
  def sectionsFromSource(source: String): Try[Map[String, Section]]

  def baseModel: Model = Model()

  def version:             ComponentSerialization[Section, Format]
  def codeComponent:       ComponentSerialization[Section, Format]
  def infoComponent:       ComponentSerialization[Section, Format]
  def interfaceComponent:  ComponentSerialization[Section, Format]
  def shapesComponent:     ComponentSerialization[Section, Format]
  def linkShapesComponent: ComponentSerialization[Section, Format]

  def defaultComponents =
    Seq(version, codeComponent, infoComponent, interfaceComponent, shapesComponent, linkShapesComponent)

  def load(source: String, optionalComponents: Seq[ComponentSerialization[Section, Format]]): Try[Model] = {
    for {
      loadedSections <- sectionsFromSource(source)
    } yield
      (defaultComponents ++ optionalComponents).foldLeft(baseModel)(addModelSection(loadedSections))
  }

  def load(location: URI, optionalComponents: Seq[ComponentSerialization[Section, Format]]): Try[Model] = {
    for {
      loadedSections <- sections(location)
    } yield
      (defaultComponents ++ optionalComponents).foldLeft(baseModel)(addModelSection(loadedSections))
  }

  def save(model: Model, uri: URI, optionalComponents: Seq[ComponentSerialization[Section, Format]]): Try[URI] = {
    val serializedSections =
      (defaultComponents ++ optionalComponents).foldLeft(Map[String, Section]())(addSerializedSection(model))
    writeSections(serializedSections, uri)
  }

  def sourceString(model: Model, optionalComponents: Seq[ComponentSerialization[Section, Format]]): Try[String] = {
    val serializedSections =
      (defaultComponents ++ optionalComponents).foldLeft(Map[String, Section]())(addSerializedSection(model))
    sectionsToSource(serializedSections)
  }

  private def addSerializedSection(model: Model)(sections: Map[String, Section], component: ComponentSerialization[Section, Format]): Map[String, Section] = {
    sections + (component.componentName -> component.serialize(model))
  }

  private def addModelSection(sections: Map[String, Section])(
    model: Model, component: ComponentSerialization[Section, Format]): Model = {
      val addComponent = sections
        .get(component.componentName)
        .map(component.deserialize _)
        .getOrElse(component.addDefault)
      addComponent(model)
  }

}

case class ModelSettings(snapToGrid: Boolean)
