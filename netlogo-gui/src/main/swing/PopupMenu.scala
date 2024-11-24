// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Insets
import javax.swing.JPopupMenu
import javax.swing.border.LineBorder

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class PopupMenu(title: String = "") extends JPopupMenu(title) with ThemeSync {
  private class Separator extends JPopupMenu.Separator with ThemeSync {
    def syncTheme() {
      setForeground(InterfaceColors.MENU_BORDER)
    }
  }

  syncTheme()

  override def addSeparator() {
    add(new Separator)
  }

  override def getInsets: Insets =
    new Insets(5, 0, 5, 0)

  def syncTheme() {
    setBackground(InterfaceColors.MENU_BACKGROUND)
    setBorder(new LineBorder(InterfaceColors.MENU_BORDER))

    getComponents.foreach(_ match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    })
  }
}
