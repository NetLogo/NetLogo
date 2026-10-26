// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ LiteralParser, Model }

trait AggregateManagerInterface extends SourceOwner with ModelSections.ModelSaveable {
  def beforeLoad(): Unit
  def load(model: Model, compiler: LiteralParser): Unit
  def isLoaded: Boolean
  def showEditor(): Unit
}
