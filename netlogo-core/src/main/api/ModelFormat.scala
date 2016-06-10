// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.net.URI

import org.nlogo.core.Model

import scala.util.{ Success, Try }

trait ComponentSerialization[A, B <: ModelFormat[A, _]] {
  def componentName: String
  def addDefault: Model => Model = identity
  def serialize(m: Model): A
  def validationErrors(m: Model): Option[String]
  def deserialize(s: A): Model => Try[Model] = (m: Model) => Success(m)
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

  def emptyModel(optionalComponents: Seq[ComponentSerialization[Section, Format]]): Model =
    (defaultComponents ++ optionalComponents).foldLeft(baseModel) {
      case (m, comp) => comp.addDefault(m)
    }

  def load(source: String, optionalComponents: Seq[ComponentSerialization[Section, Format]]): Try[Model] = {
    sectionsFromSource(source).flatMap(loadedSections =>
      (defaultComponents ++ optionalComponents).foldLeft(Try(baseModel))(addModelSection(loadedSections)))
  }

  def load(location: URI, optionalComponents: Seq[ComponentSerialization[Section, Format]]): Try[Model] = {
    sections(location).flatMap(loadedSections =>
      (defaultComponents ++ optionalComponents).foldLeft(Try(baseModel))(addModelSection(loadedSections)))
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
    modelTry: Try[Model], component: ComponentSerialization[Section, Format]): Try[Model] = {
     modelTry
        .flatMap(m =>
           sections.get(component.componentName)
             .map(sectionContents => component.deserialize(sectionContents).apply(m))
           .getOrElse(Success(component.addDefault(m))))
  }

}

case class ModelSettings(snapToGrid: Boolean)
