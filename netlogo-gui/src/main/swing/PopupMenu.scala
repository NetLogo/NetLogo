// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Insets
import java.awt.event.KeyEvent
import javax.swing.{ JPopupMenu, MenuElement, MenuSelectionManager }
import javax.swing.border.LineBorder

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class PopupMenu(title: String = "") extends JPopupMenu(title) with ThemeSync {
  private class Separator extends JPopupMenu.Separator with ThemeSync {
    override def syncTheme(): Unit = {
      setForeground(InterfaceColors.menuBorder())
    }
  }

  syncTheme()

  override def addSeparator(): Unit = {
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

class SearchablePopupMenu extends PopupMenu {
  private var search = ""
  private var lastKey = 0L

  override def processKeyEvent(e: KeyEvent, path: Array[MenuElement], manager: MenuSelectionManager): Unit = {
    if (e.getID == KeyEvent.KEY_PRESSED && e.getKeyChar.isLetterOrDigit) {
      if (System.currentTimeMillis - lastKey > 1500)
        search = ""

      search += e.getKeyChar.toLower
      lastKey = System.currentTimeMillis

      getSubElements.collect {
        case item: MenuItem =>
          Option(item.getText).map(_.filter(_.isLetterOrDigit).toLowerCase.indexOf(search))
            .filter(_ >= 0).map((item, _))
      }.flatten.minByOption(_._2).foreach { (item, _) =>
        manager.setSelectedPath(path :+ item)

        scrollRectToVisible(item.getBounds)
      }
    } else if (e.getKeyChar == KeyEvent.VK_SPACE) {
      // by default, pressing space selects the item that's currently highlighted in the menu. when searching,
      // people will probably naturally add spaces, so this makes sure those don't propagate. (Isaac B 1/12/26)
      e.consume()
    } else {
      super.processKeyEvent(e, path, manager)
    }
  }
}
