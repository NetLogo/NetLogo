// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

class Menu(text: String) extends javax.swing.JMenu(text) {
  def addMenuItem(name: String, fn: () => Unit): javax.swing.JMenuItem =
    addMenuItem(RichAction(name) { _ => fn() })
  def addMenuItem(name: String, c: Char, shifted: Boolean, fn: () => Unit): javax.swing.JMenuItem =
    addMenuItem(c, shifted, RichAction(name) { _ => fn() })
  def addMenuItem(text: String): javax.swing.JMenuItem =
    addMenuItem(text, 0.toChar, false, null: javax.swing.Action)
  def addMenuItem(text: String, shortcut: Char): javax.swing.JMenuItem =
    addMenuItem(text, shortcut, false, null: javax.swing.Action)
  def addMenuItem(action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(action.getValue(javax.swing.Action.NAME).asInstanceOf[String], action)
  def addMenuItem(text: String, action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(text, 0.toChar, false, action)
  def addMenuItem(text: String, shortcut: Char, action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(text, shortcut, false, action)
  def addMenuItem(shortcut: Char, action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(action.getValue(javax.swing.Action.NAME).asInstanceOf[String],
                shortcut, action)
  def addMenuItem(shortcut: Char, shift: Boolean, action: javax.swing.Action): javax.swing.JMenuItem =
    addMenuItem(action.getValue(javax.swing.Action.NAME).asInstanceOf[String],
                shortcut, shift, action)
  def addMenuItem(text: String, shortcut: Char, shift: Boolean): javax.swing.JMenuItem =
    addMenuItem(text, shortcut, shift, null: javax.swing.Action)
  def addMenuItem(text: String, shortcut: Char, shift: Boolean, action: javax.swing.Action): javax.swing.JMenuItem = {
    val item =
      if(action == null)
        new javax.swing.JMenuItem(text)
      else {
        val item = new javax.swing.JMenuItem(action)
        item.setText(text)
        item
      }
    val mask = if(shift) java.awt.event.InputEvent.SHIFT_MASK else 0
    if(shortcut != 0)
      item.setAccelerator(
        javax.swing.KeyStroke.getKeyStroke(
          shortcut, mask | java.awt.Toolkit.getDefaultToolkit.getMenuShortcutKeyMask))
    item.setIcon(null) // unwanted visual clutter - ST 7/31/03
    add(item)
    item
  }
  def addCheckBoxMenuItem(text: String, initialValue: Boolean, action: javax.swing.Action) = {
    val item =
      if(action == null)
        new javax.swing.JCheckBoxMenuItem(text, initialValue)
      else {
        val item = new javax.swing.JCheckBoxMenuItem(action)
        item.setText(text)
        item.setState(initialValue)
        item
      }
    item.setIcon(null) // unwanted visual clutter - ST 7/31/03
    add(item)
    item
  }
}
