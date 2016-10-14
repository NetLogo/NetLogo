// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.nlogo.core.{ LiteralParser, Model => CoreModel }
import org.nlogo.api.AutoConvertable

object SDMAutoConvertable extends AutoConvertable {
  val componentName = "org.nlogo.modelsection.systemdynamics"

  def componentDescription: String = "System Dynamics modeler"

  override def conversionSource(m: CoreModel, literalParser: LiteralParser): Option[(String, String)] = {
    m.optionalSectionValue[Model](componentName)
      .map { model =>
        "aggregate" -> new Translator(model, literalParser).source
      }
  }
}
