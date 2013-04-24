// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait AggregateManagerInterface extends SourceOwner {
  def save(): String
  def load(lines: String, parser: ParserServices)
  def showEditor()
}
