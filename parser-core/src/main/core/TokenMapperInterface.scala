// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait TokenMapperInterface {
  def getCommand(s: String): Option[Command]
  def getReporter(s: String): Option[Reporter]

  def allCommandNames: Set[String]
  def allReporterNames: Set[String]
}

