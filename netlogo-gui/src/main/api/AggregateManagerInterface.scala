// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Model

trait AggregateManagerInterface extends SourceOwner with ModelSections.ModelSaveable {
  def load(model: Model, compiler: CompilerServices)
  def isLoaded: Boolean
  def showEditor()
}
