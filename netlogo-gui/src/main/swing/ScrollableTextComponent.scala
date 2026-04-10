// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.JComponent

// used by FindDialog to center selected text (Isaac B 7/26/25)
trait ScrollableTextComponent extends JComponent {
  def isEditable: Boolean
  def getText: String
  def getSelectedText: String
  def getSelectionStart: Int
  def getSelectionEnd: Int
  def select(start: Int, end: Int): Unit
  def replaceSelection(text: String): Unit
  def scrollTo(index: Int): Unit
}
