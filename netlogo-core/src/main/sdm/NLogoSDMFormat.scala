// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.nlogo.core.{ Model => CoreModel }
import org.nlogo.fileformat.{ NLogoFormat, NLogoThreeDFormat }
import org.nlogo.api.{ ComponentSerialization, ModelFormat }

import scala.util.Try

// NOTE: If you're looking for the ComponentSerialization used
// in NetLogo-GUI, you want org.nlogo.sdm.gui.NLogoGuiSDMFormat.
// This is *only* used when loading the sdm section of the model
// headlessly. Why the difference? Headless doesn't know anything
// about the graphical-only components of the model, just sdm.Model
// GUI, meanwhile, knows about everything and deserializes an
// AggregateDrawing. - RG 5/9/16

abstract class AbstractNLogoSDMFormat[A <: ModelFormat[Array[String], A]]
  extends ComponentSerialization[Array[String], A] {
  override def componentName = "org.nlogo.modelsection.systemdynamics"
  override def addDefault = identity

  override def serialize(m: CoreModel): Array[String] =
    m.optionalSectionValue[Model](componentName)
      .map(sdm => sdm.getDt.toString + "\n" + sdm.serializedGUI)
      .toArray[String]

  override def validationErrors(m: CoreModel): Option[String] =
    None

  override def deserialize(s: Array[String]): CoreModel => Try[CoreModel] = { (m: CoreModel) =>
    Try {
      stringsToModel(s)
        .map(sdm => m.withOptionalSection[Model](componentName, Some(sdm), sdm))
        .getOrElse(m)
    }
  }

  private def stringsToModel(s: Array[String]): Option[Model] = {
    Loader.load(s.mkString("\n"))
  }
}

class NLogoSDMFormat extends AbstractNLogoSDMFormat[NLogoFormat]
class NLogoThreeDSDMFormat extends AbstractNLogoSDMFormat[NLogoThreeDFormat]
