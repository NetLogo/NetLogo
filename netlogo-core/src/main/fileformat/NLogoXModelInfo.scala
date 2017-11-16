// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  cats.data.Validated.{ Invalid, Valid }

import
  org.nlogo.core.{ model, Model, ModelInfo }, model.ModelInfoXml

import
  org.nlogo.xmllib.ElementFactory

import
  org.nlogo.api.ComponentSerialization

import
  scala.util.{ Failure, Success }

class NLogoXModelInfo(val factory: ElementFactory) extends ComponentSerialization[NLogoXFormat.Section, NLogoXFormat] {
  val componentName = ModelInfo.sectionKey
  override def addDefault = _.withOptionalSection(ModelInfo.sectionKey, None, ModelInfo.empty)
  def serialize(m: Model): NLogoXFormat.Section =
    ModelInfoXml.write(m.modelInfo, factory)
  def validationErrors(m: Model): Option[String] = None
  override def deserialize(info: NLogoXFormat.Section) = { (m: Model) =>
    (ModelInfoXml.read(info) match {
      case Valid(m: ModelInfo) => Success(m)
      case Invalid(err) => Failure(new NLogoXFormatException(err.message))
    })
      .map(info => m.withOptionalSection(ModelInfo.sectionKey, Some(info), ModelInfo.empty))

  }
}
