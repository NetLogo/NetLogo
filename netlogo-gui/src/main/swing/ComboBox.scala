// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, GridBagConstraints, GridBagLayout, Insets, ItemSelectable }
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener, MouseAdapter, MouseEvent, MouseWheelEvent,
                        MouseWheelListener }
import javax.swing.{ AbstractAction, JLabel, JPanel }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.collection.mutable.Set

object ComboBox {
  // required for custom menu item components since they are only allowed in one place (IB 11/17/24)
  trait Clone {
    def getClone: Component
  }
}

class ComboBox[T >: Null](private var items: List[T] = Nil) extends JPanel(new GridBagLayout) with RoundedBorderPanel
                                                            with ThemeSync with ItemSelectable {

  private class ChoiceDisplay extends JPanel(new GridBagLayout) with Transparent with ThemeSync {
    private val c = new GridBagConstraints

    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1

    def setItem(item: T) {
      removeAll()

      if (item != null) {
        item match {
          case comp: Component with ComboBox.Clone => add(comp.getClone, c)
          case a => add(new JLabel(a.toString), c)
        }

        syncTheme()
      }

      revalidate()
      repaint()
    }

    def syncTheme() {
      if (getComponentCount > 0) {
        getComponent(0) match {
          case ts: ThemeSync => ts.syncTheme()
          case l: JLabel => l.setForeground(InterfaceColors.TOOLBAR_TEXT)
          case _ =>
        }
      }
    }
  }

  setDiameter(6)
  enableHover()

  private var selectedItem: T = null

  private val choiceDisplay = new ChoiceDisplay
  private val arrow = new DropdownArrow

  private val popup = new PopupMenu

  private val itemListeners = Set[ItemListener]()

  locally {
    val c = new GridBagConstraints

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(3, 6, 3, 6)

    add(choiceDisplay, c)

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

    choiceDisplay.addMouseListener(mouseListener)
    choiceDisplay.addMouseWheelListener(wheelListener)

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
      choiceDisplay.setItem(null)
    }

    else {
      items.foreach(_ match {
        case c: Component =>
          popup.add(new CustomMenuItem(c, new AbstractAction {
            def actionPerformed(e: ActionEvent) {
              selectItem(c.asInstanceOf[T])
            }
          }))
        case a =>
          popup.add(new MenuItem(new AbstractAction(a.toString) {
            def actionPerformed(e: ActionEvent) {
              selectItem(a)
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
    choiceDisplay.setItem(item)

    itemListeners.foreach(_.itemStateChanged(
      new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, item, ItemEvent.SELECTED)))
  }

  def addItemListener(listener: ItemListener) {
    itemListeners += listener
  }

  def removeItemListener(listener: ItemListener) {
    itemListeners -= listener
  }

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

    choiceDisplay.syncTheme()

    popup.syncTheme()
  }
}
