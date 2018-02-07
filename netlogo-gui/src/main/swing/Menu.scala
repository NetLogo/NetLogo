// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.{ Action, JCheckBoxMenuItem, JMenu, JMenuItem }
import UserAction.{ ActionComponentKey, ActionRankKey, DefaultGroup, DefaultRank, RichUserAction }

import scala.math.Ordering

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

class Menu(text: String, var menuModel: MenuModel[Action, String]) extends JMenu(text) with UserAction.Menu {

  def this(text: String) = this(text, Menu.model)

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
        new javax.swing.JMenuItem(text)
      else {
        val item = new javax.swing.JMenuItem(action)
        item.setText(text)
        item
      }
    val mask = if(shift) java.awt.event.InputEvent.SHIFT_MASK else 0
    if(shortcut != 0) {
      val menuMask = if (addMenuMask) java.awt.Toolkit.getDefaultToolkit.getMenuShortcutKeyMask else 0
      item.setAccelerator(
        javax.swing.KeyStroke.getKeyStroke(
          shortcut, mask | menuMask))
    }
    item.setIcon(null) // unwanted visual clutter - ST 7/31/03
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
  }

  private def createMenuItem(action: Action): JMenuItem =
    action match {
      case cba: UserAction.CheckBoxAction => new JCheckBoxMenuItem(action)
      case _                              =>
        val jmi = new JMenuItem(action)
        jmi.setIcon(null)
        jmi
    }

  def revokeAction(action: Action): Unit = {
    if (action.getValue(ActionComponentKey) == this) {
      action.putValue(ActionComponentKey, null)
    }
    menuModel.removeElement(action)
    rebuildFromModel(menuModel)
  }

  def offerAction(action: Action): Unit = {
    action.putValue(ActionComponentKey, this)
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
}
