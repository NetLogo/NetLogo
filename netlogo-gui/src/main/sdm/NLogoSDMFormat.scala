// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import java.io.{ ByteArrayOutputStream, ByteArrayInputStream, BufferedReader, StringReader }

import org.jhotdraw.util.{ StorableInput, StorableOutput }

import org.nlogo.core.{ Model => CoreModel }
import org.nlogo.fileformat.NLogoFormat
import org.nlogo.api.ComponentSerialization

import scala.util.Try

// NOTE: If you're looking for the ComponentSerialization used
// in NetLogo-GUI, you want org.nlogo.sdm.gui.NLogoGuiSDMFormat.
// This is *only* used when loading the sdm section of the model
// headlessly. Why the difference? Headless doesn't know anything
// about the graphical-only components of the model, just sdm.Model
// GUI, meanwhile, knows about everything and deserializes an
// AggregateDrawing. - RG 5/9/16

class NLogoSDMFormat extends ComponentSerialization[Array[String], NLogoFormat] {
  override def componentName = "org.nlogo.modelsection.systemdynamics"
  override def addDefault = identity

  override def serialize(m: CoreModel): Array[String] =
    // unfortunately, we don't save headlessly, since there's too much graphical stuff
    Array()

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
