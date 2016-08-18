// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ LiteralParser, Model }

trait AggregateManagerInterface extends SourceOwner with ModelSections.ModelSaveable {
  def load(model: Model, literalParser: LiteralParser)
  def isLoaded: Boolean
  def showEditor()
}
