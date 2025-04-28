// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Insets
import javax.swing.JPopupMenu
import javax.swing.border.LineBorder

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class PopupMenu(title: String = "") extends JPopupMenu(title) with ThemeSync {
  private class Separator extends JPopupMenu.Separator with ThemeSync {
    override def syncTheme(): Unit = {
      setForeground(InterfaceColors.menuBorder())
    }
  }

  syncTheme()

  override def addSeparator() {
    add(new Separator)
  }

  override def getInsets: Insets =
    new Insets(5, 0, 5, 0)

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.menuBackground())
    setBorder(new LineBorder(InterfaceColors.menuBorder()))

    getComponents.foreach(_ match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    })
  }
}
