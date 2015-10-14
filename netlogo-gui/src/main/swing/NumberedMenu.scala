// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

// TODO i18n lot of work needed here...

abstract class NumberedMenu(name: String) extends Menu(name) {
  def items: Seq[(String, () => Unit)] // abstract
  for(((itemName, fn), i) <- items.zipWithIndex)
    addMenuItem(
      ('1' + i).toChar,
      new javax.swing.AbstractAction(itemName) {
        override def actionPerformed(e: java.awt.event.ActionEvent) {
          fn()
        }})
}
