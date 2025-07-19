// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.event.{ ActionEvent, InputEvent, KeyEvent }

import javax.swing.InputMap
import javax.swing.text.TextAction

import KeyBinding.{ keystroke, charKeystroke }

trait Indenter {
  def handleTab(): Unit
  def handleUntab(): Unit

  def handleCloseBracket(): Unit

  def handleInsertion(text: String): Unit

  def handleEnter(): Unit

  def enterAction: TextAction =
    new TextAction("enter") {
      def actionPerformed(e: ActionEvent): Unit = {
        handleEnter()
      }
    }

  def indentAction: TextAction =
    new TextAction("indent") {
      def actionPerformed(e: ActionEvent): Unit = {
        handleTab()
      }
    }

  def unindentAction: TextAction =
    new TextAction("unindent") {
      def actionPerformed(e: ActionEvent): Unit = {
        handleUntab()
      }
    }

  def closeBracketAction: TextAction =
    new TextAction("close-bracket") {
      def actionPerformed(e: ActionEvent): Unit = {
        handleCloseBracket()
      }
    }

  def addActions(configuration: EditorConfiguration, inputMap: InputMap): Unit = {
    inputMap.put(keystroke(KeyEvent.VK_ENTER), enterAction)
    inputMap.put(charKeystroke(']'), closeBracketAction)
    if (!configuration.enableFocusTraversal) {
      inputMap.put(keystroke(KeyEvent.VK_TAB), indentAction)
      inputMap.put(keystroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), unindentAction)
    }
  }
}
