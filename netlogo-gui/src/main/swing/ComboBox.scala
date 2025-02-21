// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension, GridBagConstraints, GridBagLayout, Insets, ItemSelectable }
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener, KeyAdapter, KeyEvent, MouseAdapter, MouseEvent,
                        MouseWheelEvent, MouseWheelListener }
import javax.swing.{ AbstractAction, JLabel, JPanel }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

object ComboBox {
  // required for custom menu item components since they are only allowed in one place (Isaac B 11/17/24)
  trait Clone {
    def getClone: Component
  }
}

class ComboBox[T](private var items: Seq[T] = Seq())
  extends JPanel(new GridBagLayout) with RoundedBorderPanel with ThemeSync with ItemSelectable {

  private val mouseListener = new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      popup.show(ComboBox.this, 0, getHeight)
    }
  }

  private val wheelListener = new MouseWheelListener {
    def mouseWheelMoved(e: MouseWheelEvent): Unit = {
      setSelectedIndex(getSelectedIndex + e.getWheelRotation)
    }
  }

  private var selectedItem: Option[T] = None

  private val choiceDisplay = new ChoiceDisplay
  private val arrow = new DropdownArrow

  private val popup = new PopupMenu {
    override def getPreferredSize: Dimension =
      new Dimension(ComboBox.this.getWidth.max(super.getPreferredSize.width), super.getPreferredSize.height)
  }

  private var itemListeners = Set[ItemListener]()

  locally {
    setFocusable(true)
    setDiameter(6)
    enableHover()

    val c = new GridBagConstraints

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(3, 6, 3, 6)

    add(choiceDisplay, c)

    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(3, 0, 3, 6)

    add(arrow, c)

    addMouseListener(mouseListener)
    addMouseWheelListener(wheelListener)

    choiceDisplay.addMouseListener(mouseListener)
    choiceDisplay.addMouseWheelListener(wheelListener)

    arrow.addMouseListener(mouseListener)
    arrow.addMouseWheelListener(wheelListener)

    addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit = {
        if (e.getKeyCode == KeyEvent.VK_DOWN)
          popup.show(ComboBox.this, 0, getHeight)
      }
    })

    setItems(items)
    syncTheme()
  }

  def setItems(items: Seq[T]): Unit = {
    this.items = items

    popup.removeAll()

    if (items.isEmpty) {
      selectedItem = None
      choiceDisplay.setItem(None)
    } else {
      items.foreach(_ match {
        case c: Component =>
          popup.add(new CustomMenuItem(c, new AbstractAction {
            def actionPerformed(e: ActionEvent): Unit = {
              selectItem(Option(c.asInstanceOf[T]))
            }
          }))
        case a =>
          popup.add(new MenuItem(new AbstractAction(a.toString) {
            def actionPerformed(e: ActionEvent): Unit = {
              selectItem(Option(a))
            }
          }))
      })

      selectItem(Option(items(0)))
    }
  }

  def setSelectedItem(item: T): Unit = {
    if (items.contains(item))
      selectItem(Option(item))
  }

  def getSelectedItem: Option[T] =
    selectedItem

  def setSelectedIndex(index: Int): Unit = {
    if (index >= 0 && index < items.size)
      selectItem(Option(items(index)))
  }

  def getSelectedIndex: Int =
    selectedItem.map(items.indexOf).getOrElse(-1)

  def clearSelection(): Unit = {
    selectItem(None)
  }

  private def selectItem(item: Option[T]): Unit = {
    selectedItem = item
    choiceDisplay.setItem(selectedItem)

    itemListeners.foreach(_.itemStateChanged(
      new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, item, ItemEvent.SELECTED)))
  }

  def addItemListener(listener: ItemListener): Unit = {
    itemListeners += listener
  }

  def removeItemListener(listener: ItemListener): Unit = {
    itemListeners -= listener
  }

  // required by ItemSelectable, but not used by NetLogo code
  // unimplemented because T can't be interpreted as Object
  // (Isaac B 2/8/25)
  override def getSelectedObjects: Array[AnyRef] = ???

  def itemCount: Int =
    items.size

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.toolbarControlBackground)
    setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover)
    setBorderColor(InterfaceColors.toolbarControlBorder)

    choiceDisplay.syncTheme()

    popup.syncTheme()
  }

  private class ChoiceDisplay extends JPanel(new GridBagLayout) with Transparent with ThemeSync {
    private val c = new GridBagConstraints

    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1

    def setItem(item: Option[T]): Unit = {
      removeAll()

      item.foreach(_ match {
        case comp: Component with ComboBox.Clone =>
          val child = comp.getClone

          add(child, c)

          child.addMouseListener(mouseListener)
          child.addMouseWheelListener(wheelListener)

        case a =>
          val child = new JLabel(a.toString)

          add(child, c)

          child.addMouseListener(mouseListener)
          child.addMouseWheelListener(wheelListener)
      })

      syncTheme()
      revalidate()
      repaint()
    }

    override def syncTheme(): Unit = {
      if (getComponentCount > 0) {
        getComponent(0) match {
          case ts: ThemeSync => ts.syncTheme()
          case l: JLabel => l.setForeground(InterfaceColors.toolbarText)
          case _ =>
        }
      }
    }
  }
}
