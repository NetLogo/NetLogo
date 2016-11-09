// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.Action

import org.nlogo.window.Event.LinkChild
import org.nlogo.api.ModelSections.ModelSaveable

trait LabManagerInterface extends LinkChild with ModelSaveable {
  def show(): Unit

  def actions: Seq[Action]
}
