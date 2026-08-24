// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension, ItemSelectable }
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener, KeyAdapter, KeyEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, Box, BoxLayout, JLabel, JPanel }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

object ComboBox {
  // required for custom menu item components since they are only allowed in one place (Isaac B 11/17/24)
  trait Clone extends Component {
    def getClone: Component
  }
}

class ComboBox[T](private var items: Seq[T] = Seq(), openOnPress: Boolean = true, searchable: Boolean = false)
  extends JPanel with RoundedBorderPanel with Zoomable with ThemeSync with ItemSelectable {

  // popups with lots of items can overlap the mouse when the dropdown is clicked, causing one of
  // the items to be erroneously selected when the mouse is released. this makes it difficult to
  // interact with the popup, so wait until the mouse is released in those cases. (Isaac B 1/12/26)
  private val mouseAdapter: MouseAdapter = {
    if (openOnPress) {
      new MouseAdapter {
        override def mousePressed(e: MouseEvent): Unit = {
          if (isEnabled)
            showPopup()
        }
      }
    } else {
      new MouseAdapter {
        override def mouseReleased(e: MouseEvent): Unit = {
          if (isEnabled)
            showPopup()
        }
      }
    }
  }

  private var selectedItem: Option[T] = None

  private val choiceDisplay = new ChoiceDisplay
  private val arrow = new DropdownArrow {
    override def getPreferredSize: Dimension = {
      // the arrow looks uneven if the width is even, so make sure it's odd after scaling
      // to the zoomed value (Isaac B 3/19/26)
      val arrowWidth: Int = {
        val width = Utils.zoom(9)

        if (width % 2 == 0) {
          width + 1
        } else {
          width
        }
      }

      new Dimension(arrowWidth, (arrowWidth / 1.8).ceil.toInt)
    }
  }

  private var itemListeners = Set[ItemListener]()

  setDiameter(Utils.zoom(6))
  setFocusable(true)
  enableHover()

  addMouseListener(mouseAdapter)
  choiceDisplay.addMouseListener(mouseAdapter)
  arrow.addMouseListener(mouseAdapter)

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit = {
      if (e.getKeyCode == KeyEvent.VK_DOWN && isEnabled)
        showPopup()
    }
  })

  setItems(items)

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  setBorder(new ZoomableBorder(3, 6, 3, 6))

  add(choiceDisplay)
  add(new HorizontalStrut(6))
  add(Box.createHorizontalGlue)
  add(arrow)

  syncTheme()

  protected def getPopup: PopupMenu = {
    val popup: PopupMenu = {
      if (searchable) {
        new SearchablePopupMenu {
          override def getPreferredSize: Dimension =
            new Dimension(ComboBox.this.getWidth.max(super.getPreferredSize.width), super.getPreferredSize.height)
        }
      } else {
        new PopupMenu {
          override def getPreferredSize: Dimension =
            new Dimension(ComboBox.this.getWidth.max(super.getPreferredSize.width), super.getPreferredSize.height)
        }
      }
    }

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

    popup
  }

  def showPopup(): Unit = {
    getPopup.show(this, 0, getHeight)
  }

  def setItems(items: Seq[T]): Unit = {
    this.items = items

    if (items.isEmpty) {
      selectedItem = None
      choiceDisplay.setItem(None)
    } else {
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

  override def setEnabled(enabled: Boolean): Unit = {
    super.setEnabled(enabled)

    arrow.setEnabled(enabled)
    choiceDisplay.setEnabled(enabled)
  }

  override def getMaximumSize: Dimension =
    new Dimension(super.getMaximumSize.width, getPreferredSize.height)

  override def zoom(oldZoom: Float): Unit = {
    setDiameter(Utils.zoom(6))
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.toolbarControlBackground())
    setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
    setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
    setBorderColor(InterfaceColors.toolbarControlBorder())

    choiceDisplay.syncTheme()
  }

  private class ChoiceDisplay extends JPanel with Transparent with ThemeSync {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

    def setItem(item: Option[T]): Unit = {
      removeAll()

      item.map {
        case comp: ComboBox.Clone =>
          comp.getClone

        case a =>
          new JLabel(a.toString) {
            setFont(ChoiceDisplay.this.getFont)
          }
      }.foreach { child =>
        add(child)

        child.addMouseListener(mouseAdapter)
      }

      syncTheme()
      revalidate()
      repaint()
    }

    override def setEnabled(enabled: Boolean): Unit = {
      super.setEnabled(enabled)

      getComponents.foreach(_.setEnabled(enabled))
    }

    override def syncTheme(): Unit = {
      if (getComponentCount > 0) {
        getComponent(0) match {
          case ts: ThemeSync => ts.syncTheme()
          case l: JLabel => l.setForeground(InterfaceColors.toolbarText())
          case _ =>
        }
      }
    }
  }
}
