// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ GridBagConstraints, GridBagLayout, Insets, ItemSelectable }
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener, MouseAdapter, MouseEvent, MouseWheelEvent,
                        MouseWheelListener }
import javax.swing.{ AbstractAction, JLabel, JPanel, JPopupMenu }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.collection.mutable.Set

class ComboBox[T >: Null](private var items: List[T] = Nil) extends JPanel(new GridBagLayout) with RoundedBorderPanel
                                                            with ThemeSync with ItemSelectable {
  setDiameter(6)
  enableHover()

  private var selectedItem: T = null

  private val label = new JLabel
  private val arrow = new DropdownArrow

  private val popup = new JPopupMenu

  private val itemListeners = Set[ItemListener]()

  locally {
    val c = new GridBagConstraints

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(3, 6, 3, 6)

    add(label, c)

    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(3, 0, 3, 6)

    add(arrow, c)

    val mouseListener = new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        popup.show(ComboBox.this, 0, getHeight)
      }
    }

    val wheelListener = new MouseWheelListener {
      def mouseWheelMoved(e: MouseWheelEvent) {
        setSelectedIndex(getSelectedIndex + e.getWheelRotation)
      }
    }

    addMouseListener(mouseListener)
    addMouseWheelListener(wheelListener)

    label.addMouseListener(mouseListener)
    label.addMouseWheelListener(wheelListener)

    arrow.addMouseListener(mouseListener)
    arrow.addMouseWheelListener(wheelListener)
  }

  setItems(items)
  syncTheme()

  def setItems(items: List[T]) {
    this.items = items

    popup.removeAll()

    if (items.isEmpty) {
      selectedItem = null
      label.setText("")
    }

    else {
      items.foreach(item => {
        popup.add(new MenuItem(new AbstractAction(item.toString) {
          def actionPerformed(e: ActionEvent) {
            selectItem(item)
          }
        }))
      })

      selectItem(items(0))
    }
  }

  def setSelectedItem(item: T) {
    if (items.contains(item))
      selectItem(item)
  }

  def getSelectedItem: T =
    selectedItem
  
  def setSelectedIndex(index: Int) {
    if (index >= 0 && index < items.size)
      selectItem(items(index))
  }

  def getSelectedIndex: Int =
    items.indexOf(selectedItem)
  
  private def selectItem(item: T) {
    selectedItem = item
    label.setText(item.toString)

    itemListeners.foreach(_.itemStateChanged(
      new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, item, ItemEvent.SELECTED)))
  }

  def addItemListener(listener: ItemListener) {
    itemListeners += listener
  }

  def removeItemListener(listener: ItemListener) {
    itemListeners -= listener
  }

  // required to implement ItemSelectable, but not used in NetLogo (IB 11/12/24)
  def getSelectedObjects: Array[Object] = {
    selectedItem match {
      case obj: Object => Array(obj)
      case _ => null
    }
  }
  
  def syncTheme() {
    setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
    setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)

    label.setForeground(InterfaceColors.TOOLBAR_TEXT)

    popup.setBackground(InterfaceColors.MENU_BACKGROUND)

    popup.getComponents.foreach(_ match {
      case ts: ThemeSync => ts.syncTheme()
    })
  }
}
