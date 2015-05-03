// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait LabManagerInterface extends Event.LinkChild {
  def show(): Unit
  def save(): String
}
