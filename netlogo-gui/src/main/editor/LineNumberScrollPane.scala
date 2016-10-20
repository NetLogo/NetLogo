// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Component, Font }

import javax.swing.JScrollPane
import javax.swing.text.JTextComponent

class LineNumberScrollPane(text: JTextComponent, vsbPolicy: Int, hsbPolicy: Int)
  extends JScrollPane(text, vsbPolicy, hsbPolicy)
  with EditorScrollPane {

  lazy val lineNumbers = new LineNumbersBar(text)

  def setLineNumbersEnabled(enabled: Boolean) = {
    setRowHeaderView(if(enabled) lineNumbers else null)
  }

  def lineNumbersEnabled: Boolean =
    getRowHeader.getView != null

  override def setFont(font: Font) = {
    super.setFont(font)
    lineNumbers.setFont(font)
  }
}
