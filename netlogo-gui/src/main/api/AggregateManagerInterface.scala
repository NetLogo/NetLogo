// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api
trait AggregateManagerInterface extends SourceOwner with ModelSections.Saveable {
  def save():String
  def load(lines:String,compiler:CompilerServices)
  def showEditor()
}
