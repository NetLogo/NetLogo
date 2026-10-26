// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Model

trait ProceduresInterface extends SourceOwner {
  def loadModel(model: Model): Unit
}
