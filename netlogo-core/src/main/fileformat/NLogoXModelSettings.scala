// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  cats.data.Validated.{ Invalid, Valid }

import
  org.nlogo.core.{ model, Model },
    model.ElementFactory

import
  org.nlogo.api.{ ComponentSerialization, ModelSettings }

import
  scala.util.{ Failure, Success }

class NLogoXModelSettings(factory: ElementFactory) extends ComponentSerialization[NLogoXFormat.Section, NLogoXFormat] {
  val componentName = "org.nlogo.modelsection.modelsettings"
  override def addDefault = { (m: Model) =>
    m.withOptionalSection(componentName, None, ModelSettings(false))
  }
  def serialize(m: Model): NLogoXFormat.Section = {
    val settings =
      m.optionalSectionValue[ModelSettings](componentName)
        .getOrElse(ModelSettings(false))
    ModelSettingsXml.write(settings, factory)
  }
  def validationErrors(m: Model): Option[String] = None
  override def deserialize(s: NLogoXFormat.Section) = {(m: Model) =>
    ModelSettingsXml.read(s) match {
      case Valid(s) => Success(m.withOptionalSection(componentName, Some(s), ModelSettings(false)))
      case Invalid(err) => Failure(new NLogoXFormatException(err.message))
    }
  }
}
