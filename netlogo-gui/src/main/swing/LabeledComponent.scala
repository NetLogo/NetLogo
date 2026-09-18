// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Color
import javax.swing.{ JComponent, JLabel }

import org.nlogo.theme.ThemeSync

class LabeledComponent(text: String, component: JComponent & ThemeSync) extends BoxRow(6) with ThemeSync {
  // if this isn't lazy, setForeground gets called too early and throws an exception (Isaac B 2/15/25)
  private lazy val label = new JLabel(text) with Zoomable

  add(label)
  add(component)

  syncTheme()

  override def setForeground(color: Color): Unit = {
    label.setForeground(color)
  }

  override def syncTheme(): Unit = {
    component.syncTheme()
  }
}
