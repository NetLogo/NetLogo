// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Token

@deprecated("6.0.3", "Use core.TokenHolder instead")
trait TokenHolder {
  def token(t:Token): Unit
}
