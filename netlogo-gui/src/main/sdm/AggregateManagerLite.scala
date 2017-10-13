// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.nlogo.core.{ LiteralParser, Model => CoreModel }
import org.nlogo.api.AggregateManagerInterface

class AggregateManagerLite extends AggregateManagerInterface {

  private def unsupported = throw new UnsupportedOperationException

  def load(model: CoreModel, compiler: LiteralParser) {
    model.optionalSectionValue[Model]("org.nlogo.modelsection.systemdynamics")
      .foreach { (m: Model) =>
        source = new Translator(m, compiler).source
      }
  }

  /// implementations of SourceOwner methods
  var source = ""
  override def innerSource = source
  override def innerSource_=(s: String) = unsupported
  override def isLoaded = source.nonEmpty
  def classDisplayName = "System Dynamics"
  def kind = unsupported
  def headerSource = ""

  /// these AggregateManagerInterface methods aren't relevant when running headless
  def showEditor() = unsupported
  def updateModel(m: CoreModel): CoreModel = m

}
