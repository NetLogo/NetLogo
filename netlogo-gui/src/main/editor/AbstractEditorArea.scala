// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.event.TextListener
import javax.swing.JComponent

trait AbstractEditorArea extends JComponent {
  def setIndenter(smart: Boolean): Unit

  def setSelection(s: Boolean): Unit

  def selectNormal(): Unit
  def selectError(start: Int, end: Int): Unit

  def addTextListener(listener: TextListener): Unit

  def getText: String
  def setText(text: String): Unit
  def select(start: Int, end: Int): Unit
  def getCaretPosition: Int
  def getSelectionEnd: Int
  def getTokenAtCaret: Option[String]
}
