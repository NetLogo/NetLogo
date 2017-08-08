// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import
  cats.data.Validated.{ Invalid, Valid }

import
  org.nlogo.core.{ model, Model => CoreModel },
    model.{ Element, ElementFactory, MissingElement, XmlReader }

import
  org.nlogo.fileformat.{ NLogoXFormat, NLogoXFormatException }

import
  org.nlogo.api.ComponentSerialization

import scala.util.{ Failure, Success, Try }

// NOTE: If you're looking for the ComponentSerialization used
// in NetLogo-GUI, you want org.nlogo.sdm.gui.NLogoXGuiSDMFormat.
// This is *only* used when loading the sdm section of the model
// headlessly. Why the difference? Headless doesn't know anything
// about the graphical-only components of the model, just sdm.Model
// GUI, meanwhile, knows about everything and deserializes an
// AggregateDrawing. - RG 9/19/17

class NLogoXSDMFormat(factory: ElementFactory) extends ComponentSerialization[NLogoXFormat.Section, NLogoXFormat] {
  override def componentName = "org.nlogo.modelsection.systemdynamics"
  override def addDefault = identity

  override def serialize(m: CoreModel): NLogoXFormat.Section =
    // unfortunately, we don't save headlessly, since there's too much graphical stuff
    factory.newElement("systemDynamics").build

  override def validationErrors(m: CoreModel): Option[String] =
    None

  override def deserialize(e: Element): CoreModel => Try[CoreModel] = { (m: CoreModel) =>
    XmlReader.allElementReader("jhotdraw6").read(e).map(XmlReader.childText _) match {
      case Valid(sdm)   =>
        Try(stringsToModel(sdm.lines.toArray[String]))
          .map(sdmModelOpt => sdmModelOpt
            .map(sdmModel => m.withOptionalSection[Model](componentName, Some(sdmModel), sdmModel)))
          .map(_.getOrElse(m))
      case Invalid(_: MissingElement) => Success(m)
      case Invalid(err) => Failure(new NLogoXFormatException(err.message))
    }
  }

  private def stringsToModel(s: Array[String]): Option[Model] = {
    Loader.load(s.mkString("\n"))
  }
}
