// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait TokenHolder {
  def token: Token
  def token_=(t: Token): Unit
}
