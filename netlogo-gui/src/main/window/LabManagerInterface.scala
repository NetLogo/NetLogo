// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.window.Event.LinkChild
import org.nlogo.api.ModelSections.Saveable

trait LabManagerInterface extends LinkChild with Saveable {
  def show(): Unit

  def save: String
}
