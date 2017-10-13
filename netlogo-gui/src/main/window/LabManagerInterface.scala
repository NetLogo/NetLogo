// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.Action

import org.nlogo.window.Event.LinkChild

trait LabManagerInterface extends LinkChild {
  def show(): Unit

  def actions: Seq[Action]
}
