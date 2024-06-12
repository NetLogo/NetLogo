// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.{JComboBox, JList, JMenuItem, ListCellRenderer}
import java.awt.{Color, Component}

class ToolBarComboBoxRenderer extends JMenuItem with ListCellRenderer[JMenuItem] {
  def getListCellRendererComponent(list: JList[_ <: JMenuItem], item: JMenuItem, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
    if (isSelected) {
      setBackground(list.getSelectionBackground)
    }

    else {
      setBackground(list.getBackground)
    }

    if (item.isEnabled) {
      setForeground(item.getForeground)
      setIcon(item.getIcon)
    }

    else {
      setForeground(Color.gray)
      setIcon(item.getDisabledIcon)
    }

    setText(item.getText)

    this
  }
}

class ToolBarComboBox(val items: Array[JMenuItem]) extends JComboBox[JMenuItem](items) {
  var chosenItem: JMenuItem = items(0)

  locally {
    org.nlogo.awt.Fonts.adjustDefaultFont(this)

    setRenderer(new ToolBarComboBoxRenderer())
  }

  override def setSelectedItem(o: Object) {
    if (o.asInstanceOf[JMenuItem].isEnabled) {
      super.setSelectedItem(o)

      chosenItem = o.asInstanceOf[JMenuItem]
    }
  }

  def updateList(canAddWidget: String => Boolean) {
    for (item <- items) {
      item.setEnabled(canAddWidget(item.getText))
    }

    if (!getSelectedItem.asInstanceOf[JMenuItem].isEnabled) {
      super.setSelectedItem(getItemAt(0))
    }
  }
}
