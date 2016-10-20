// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.{ KeyStroke }

object KeyBinding {
  def keystroke(key: Int, mask: Int = 0): KeyStroke =
    KeyStroke.getKeyStroke(key, mask)

  def charKeystroke(char: Char, mask: Int = 0): KeyStroke =
    KeyStroke.getKeyStroke(Character.valueOf(char), mask)

  def menuShortcutMask = java.awt.Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
}
