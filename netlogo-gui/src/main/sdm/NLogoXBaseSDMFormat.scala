// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import
  org.nlogo.core.{ Model => CoreModel }

import
  org.nlogo.api.ComponentSerialization

import
  org.nlogo.fileformat.{ NLogoXFormat, NLogoXFormatException }

import
  org.nlogo.xmllib.{ Element, ElementFactory, MissingElement, ParseError, XmlReader }

import
  cats.{ Apply, data },
    data.Validated,
      Validated.{ Invalid, Valid }

import
  scala.util.{ Failure, Success, Try }

// this trait contains the shared logic between NLogoXSDMFormat, which deserializes to
// an sdm.Model optional model section, and NLogoXGuiSDMFormat, which deserializes to
// an AggregateDrawing model section.
trait NLogoXBaseSDMFormat[A <: AnyRef] extends ComponentSerialization[NLogoXFormat.Section, NLogoXFormat] {
  def factory: ElementFactory
  override def componentName = "org.nlogo.modelsection.systemdynamics"
  override def addDefault = identity

  override def validationErrors(m: CoreModel): Option[String] =
    None

  override def serialize(m: CoreModel): NLogoXFormat.Section =
    m.optionalSectionValue[A](componentName)
      .map(section => (objectToStrings(section), objectToDt(section)))
      .map {
        case (strings, dt) =>
          factory.newElement("systemDynamics")
            .withAttribute("dt", dt.toString)
            .withElement(factory.newElement("jhotdraw6").withText(strings).build)
            .build
      }
      .getOrElse(factory.newElement("systemDynamics").build)

  override def deserialize(e: NLogoXFormat.Section): CoreModel => Try[CoreModel] = { (m: CoreModel) =>
    reader.read(e) match {
      case Valid(aOption) =>
        Try(aOption.map(a => m.withOptionalSection(componentName, Some(a), a)).getOrElse(m))
      case Invalid(MissingElement("jhotdraw6")) => Success(m)
      case Invalid(err) => Failure(new NLogoXFormatException(err.message))
    }
  }

  // reads in dt, GUI Strings
  val reader: XmlReader[Element, Option[A]] = {
    new XmlReader[Element, Option[A]] {
      type ReadValidation = ({ type l[B] = Validated[ParseError, B] })
      val dtReader = XmlReader.doubleReader("dt")
      val jhotdrawReader = XmlReader.allElementReader("jhotdraw6").map(XmlReader.childText _)
      def name: String = s"(${dtReader.name}, ${jhotdrawReader.name})"

      def read(elem: Element): Validated[ParseError, Option[A]] = {
        Apply[ReadValidation#l].map2(dtReader.read(elem), jhotdrawReader.read(elem)) {
          case (dt, jhd) => stringsToObject(dt, jhd)
        }
      }
    }
  }

  def stringsToObject(dt: Double, jhotdrawLines: String): Option[A]
  def objectToStrings(a: A): String
  def objectToDt(a: A): Double
}
