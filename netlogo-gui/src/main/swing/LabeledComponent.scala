// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JComponent, JLabel, JPanel }

import org.nlogo.theme.ThemeSync

class LabeledComponent(text: String, component: JComponent with ThemeSync)
  extends JPanel(new GridBagLayout) with Transparent with ThemeSync {

  // if this isn't lazy, setForeground gets called too early and throws an exception (Isaac B 2/15/25)
  private lazy val label = new JLabel(text)

  locally {
    val c = new GridBagConstraints

    c.insets = new Insets(0, 0, 0, 6)

    add(label, c)

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(0, 0, 0, 0)

    add(component, c)

    syncTheme()
  }

  override def setForeground(color: Color): Unit = {
    label.setForeground(color)
  }

  override def syncTheme(): Unit = {
    component.syncTheme()
  }
}
