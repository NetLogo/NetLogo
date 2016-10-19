// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import org.nlogo.core.TokenType

import javax.swing._
import javax.swing.text.TextAction

class EditorField(
  columns: Int,
  font: java.awt.Font,
  enableFocusTraversalKeys: Boolean,
  colorizer: Colorizer,
  actionMap: Map[KeyStroke, TextAction] = EditorArea.emptyMap,
  menuItems: Seq[Action] = Seq[Action]())
  extends EditorArea(
    EditorConfiguration.default(1, columns, colorizer)
    .withFont(font)
    .withFocusTraversalEnabled(enableFocusTraversalKeys)
    .withKeymap(actionMap)
    .withContextActions(menuItems)) {

  // shut off the default actions for some keystrokes... let
  // someone add a KeyListener if they want - ST 7/30/03
  getInputMap().remove(javax.swing.KeyStroke.getKeyStroke
      (java.awt.event.KeyEvent.VK_ENTER, 0))

  override def replaceSelection(cont: String): Unit = {
    // you wouldn't think content would ever be null, but on Mac OS X
    // we've found that typing the dead-key combinations for adding
    // accents to letters causes an exception here unless we do a
    // null check (bug #1079) - ST 10/12/10
    var content = cont
    if (content != null) {
      var index = content.indexOf('\n')
      if (index != -1) {
        content = content.substring(0, index)
      }
      index = content.indexOf('\r')
      if (index != -1) {
        content = content.substring(0, index)
      }
    }
    super.replaceSelection(content)
  }
}
