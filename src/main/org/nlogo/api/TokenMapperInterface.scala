// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait TokenMapperInterface {
  def isCommand(s: String): Boolean
  def isReporter(s: String): Boolean
  // caller's responsibility to validate input for these two
  def getCommand(s: String): TokenHolder
  def getReporter(s: String): TokenHolder
  // for unit testing
  def allCommandClassNames: Set[String]
  def allReporterClassNames: Set[String]
}
