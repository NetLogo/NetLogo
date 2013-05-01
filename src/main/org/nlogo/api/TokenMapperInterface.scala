// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait TokenMapperInterface {
  def getCommand(s: String): Option[TokenHolder]
  def getReporter(s: String): Option[TokenHolder]
}
