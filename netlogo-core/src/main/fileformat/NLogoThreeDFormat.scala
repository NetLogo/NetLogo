// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.ModelFormat
import org.nlogo.core.{ Model, WorldDimensions3D }
import scala.util.{ Failure, Success }
import org.nlogo.core.model.WidgetReader

class NLogoThreeDFormat
  extends ModelFormat[Array[String], NLogoThreeDFormat]
  with AbstractNLogoFormat[NLogoThreeDFormat] {
  val is3DFormat = true
  def name: String = "nlogo3d"
  override def widgetReaders =
    Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)

  override def isCompatible(location: java.net.URI): Boolean =
    sections(location) match {
      case Success(sections) =>
        sections("org.nlogo.modelsection.version")
          .find(_.contains("NetLogo 3D"))
          .flatMap(_ => Some(true)).getOrElse(false)
      case Failure(ex) => false
    }
  override def isCompatible(source: String): Boolean =
    sectionsFromSource(source) match {
      case Success(sections) =>
        sections("org.nlogo.modelsection.version")
          .find(_.contains("NetLogo 3D"))
          .flatMap(_ => Some(true)).getOrElse(false)
      case Failure(ex) => false
    }
  override def isCompatible(model: Model): Boolean =
    model.version.contains("3D")
  lazy val defaultView = Model.defaultView.copy(dimensions = new WorldDimensions3D(-16, 16, -16, 16, -16, 16, 13.0))
}
