// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.core.{ LiteralParser, Model => CoreModel }
import org.nlogo.api.AutoConvertable
import org.nlogo.sdm.Translator

object SDMGuiAutoConvertable extends AutoConvertable {
  val componentName = "org.nlogo.modelsection.systemdynamics"

  override def conversionSource(m: CoreModel, literalParser: LiteralParser): Option[(String, String)] = {
    m.optionalSectionValue[AggregateDrawing](componentName)
      .map { drawing =>
        val sdmModel = drawing.getModel
        "aggregate" -> new Translator(sdmModel, literalParser).source
      }
  }
}
