// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait ParserServices {
  def readFromString(s: String): AnyRef
  def readNumberFromString(source: String): AnyRef
  def isReporter(s: String): Boolean
}
