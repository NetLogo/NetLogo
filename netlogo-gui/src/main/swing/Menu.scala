// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import javax.swing.{ Action, Icon, JMenu, JMenuItem, JPopupMenu }
import javax.swing.border.LineBorder
import javax.swing.plaf.basic.BasicMenuUI

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.math.Ordering

import UserAction._

object Menu {
  object MenuOrdering extends scala.math.Ordering[Action] {
    private def getRank(a: Action): Double =
      a.getValue(ActionRankKey) match {
        case d: java.lang.Double => d.doubleValue
        case _ => DefaultRank
      }

    def compare(x: Action, y: Action): Int =
      implicitly[Ordering[Double]].compare(getRank(x), getRank(y))
  }

  implicit val menuOrdering = Menu.MenuOrdering

  def model = new MenuModel[Action, String]

  def model(sortOrder: Seq[String]) = new MenuModel[Action, String](sortOrder)
}

class Menu(text: String, var menuModel: MenuModel[Action, String]) extends JMenu(text) with UserAction.Menu
                                                                   with ThemeSync {

  def this(text: String) = this(text, Menu.model)

  private val menuUI = new BasicMenuUI with ThemeSync {
    arrowIcon = new Icon {
      override def getIconWidth: Int = 5
      override def getIconHeight: Int = 9

      override def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
        val g2d = Utils.initGraphics2D(g)

        if (isSelected) {
          g2d.setColor(InterfaceColors.toolbarTextSelected())
        } else {
          g2d.setColor(InterfaceColors.toolbarText())
        }

        g2d.drawLine(x, y + 1, x + getIconWidth - 1, y + getIconHeight / 2 + 1)
        g2d.drawLine(x + getIconWidth - 1, y + getIconHeight / 2 + 1, x, y + getIconHeight)
      }
    }

    override def syncTheme(): Unit = {
      setForeground(InterfaceColors.toolbarText())

      selectionBackground = InterfaceColors.menuBackgroundHover()
      selectionForeground = InterfaceColors.menuTextHover()
      acceleratorForeground = InterfaceColors.toolbarText()
      acceleratorSelectionForeground = InterfaceColors.menuTextHover()
      disabledForeground = InterfaceColors.menuTextDisabled()
    }
  }

  setUI(menuUI)
  syncTheme()

  override def getPopupMenu: JPopupMenu = {
    val menu = super.getPopupMenu

    menu.setBackground(InterfaceColors.menuBackground())
    menu.setBorder(new LineBorder(InterfaceColors.menuBorder()))

    menu
  }

  def addMenuItem(name: String, fn: () => Unit): javax.swing.JMenuItem =
    addMenuItem(RichAction(name) { _ => fn() })
  def addMenuItem(name: String, c: Char, shifted: Boolean, fn: () => Unit): javax.swing.JMenuItem =
    addMenuItem(c, shifted, RichAction(name) { _ => fn() })
  def addMenuItem(text: String): javax.swing.JMenuItem =
    addMenuItem(text, 0.toChar, false, null: javax.swing.Action, true)
  def addMenuItem(text: String, shortcut: Char): javax.swing.JMenuItem =
    addMenuItem(text, shortcut, false, null: javax.swing.Action, true)
  def addMenuItem(action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(action.getValue(javax.swing.Action.NAME).asInstanceOf[String], action)
  def addMenuItem(text: String, action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(text, 0.toChar, false, action, true)
  def addMenuItem(text: String, shortcut: Char, action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(text, shortcut, false, action, true)
  def addMenuItem(text: String, shortcut: Char, action: javax.swing.Action, addMenuMask: Boolean): javax.swing.JMenuItem =
    addMenuItem(text, shortcut, false, action, addMenuMask)
  def addMenuItem(shortcut: Char, action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(action.getValue(javax.swing.Action.NAME).asInstanceOf[String],
                shortcut, action, true)
  def addMenuItem(shortcut: Char, shift: Boolean, action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(action.getValue(javax.swing.Action.NAME).asInstanceOf[String],
                shortcut, shift, action, true)
  def addMenuItem(text: String, shortcut: Char, shift: Boolean): javax.swing.JMenuItem =
    addMenuItem(text, shortcut, shift, null: javax.swing.Action, true)
  def addMenuItem(text: String, shortcut: Char, shift: Boolean, action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(text, shortcut, shift, action, true)
  def addMenuItem(text: String, shortcut: Char, shift: Boolean, action: javax.swing.Action, addMenuMask: Boolean): javax.swing.JMenuItem = {
    val item =
      if(action == null)
        new MenuItem(text, false)
      else {
        val item = new MenuItem(action, false)
        item.setText(text)
        item
      }
    val mask = if(shift) java.awt.event.InputEvent.SHIFT_DOWN_MASK else 0
    if(shortcut != 0) {
      val menuMask = if (addMenuMask) java.awt.Toolkit.getDefaultToolkit.getMenuShortcutKeyMaskEx else 0
      item.setAccelerator(
        javax.swing.KeyStroke.getKeyStroke(
          shortcut, mask | menuMask))
    }
    add(item)
    item
  }

  protected var groups: Map[String, Range] = Map()
  protected var subcategories: Map[String, (Menu, String)] = Map()

  def rebuildFromModel(mm: MenuModel[Action, String]): Unit = {
    getMenuComponents.foreach(remove(_))
    mm.children.foldLeft(Option.empty[String]) {
      case (priorGroup, node) =>
        if (priorGroup.nonEmpty && ! priorGroup.exists(_ == node.groupName)) {
          addSeparator()
        }
        val itemToAdd = node match {
          case mm.Branch(model, key, group) =>
            val subMenu = new Menu(subcategoryNameAndGroup(key)._1, model)
            subMenu.rebuildFromModel(subMenu.menuModel)
            subMenu
          case mm.Leaf(action, group) => createMenuItem(action)
        }
        add(itemToAdd)
        Some(node.groupName)
    }
    syncTheme()
  }

  private def createMenuItem(action: Action): JMenuItem =
    action match {
      case cba: UserAction.CheckBoxAction => new PopupCheckBoxMenuItem(action)
      case _                              =>
        new MenuItem(action, false)
    }

  def revokeAction(action: Action): Unit = {
    menuModel.removeElement(action)
    rebuildFromModel(menuModel)
  }

  def offerAction(action: Action): Unit = {
    subcategoryItem(action) match {
      case Some((subcategoryKey, subcategoryGroup)) =>
        val branch = menuModel.createBranch(subcategoryKey, subcategoryGroup)
        branch.insertLeaf(action, action.group)
      case None                  =>
        menuModel.insertLeaf(action, action.group)
    }
    rebuildFromModel(menuModel)
  }

  def subcategoryItem(action: Action): Option[(String, String)] = {
    action.getValue(UserAction.ActionSubcategoryKey) match {
      case key: String =>
        val (_, group) = subcategoryNameAndGroup(key)
        Some((key, group))
      case _ => None
    }
  }

  protected def subcategoryNameAndGroup(key: String): (String, String) = {
    (key, DefaultGroup)
  }

  override def syncTheme(): Unit = {
    menuUI.syncTheme()

    getMenuComponents.foreach(_ match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    })
  }
}
