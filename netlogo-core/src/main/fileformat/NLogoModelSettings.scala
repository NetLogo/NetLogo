// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.Model
import org.nlogo.api.{ ComponentSerialization, ModelSettings, ModelFormat }

import scala.util.Try

trait ModelSettingsComponent[A <: ModelFormat[Array[String], A]] extends ComponentSerialization[Array[String], A] {
  val componentName = "org.nlogo.modelsection.modelsettings"
  override def addDefault = { (m: Model) =>
    m.withOptionalSection(componentName, None, ModelSettings(false))
  }
  def serialize(m: Model): Array[String] = {
    val line =
      m.optionalSectionValue[ModelSettings](componentName)
        .map(s => if (s.snapToGrid) "1" else "0")
        .getOrElse("0")
    Array(line)
  }
  def validationErrors(m: Model): Option[String] = None
  override def deserialize(s: Array[String]) = {(m: Model) =>
    Try {
      val foundValue =
        ModelSettings(s.headOption.map(_.toInt != 0).getOrElse(false))
      m.withOptionalSection(componentName, Some(foundValue), ModelSettings(false))
    }
  }
}

object NLogoModelSettings extends ModelSettingsComponent[NLogoFormat]
object NLogoThreeDModelSettings extends ModelSettingsComponent[NLogoThreeDFormat]
