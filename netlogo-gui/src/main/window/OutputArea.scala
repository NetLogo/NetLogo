// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Component, Dimension, EventQueue, Font, FontMetrics }
import java.io.IOException
import javax.swing.{ JPanel, JScrollPane, JTextArea, ScrollPaneConstants }

import org.nlogo.core.{ I18N, FileMode }
import org.nlogo.api.Exceptions
import org.nlogo.awt.{ Fonts => NLogoFonts, LineBreaker }
import org.nlogo.agent.OutputObject

object OutputArea {
  private val PreferredWidth = 200
  private val PreferredHeight = 45
  private val MinimumWidth = 50
  private val GuessScrollBarWidth = 24
  class DefaultTextArea extends JTextArea {
    override def getMinimumSize: Dimension = new Dimension(50, (getRowHeight * 1.25).toInt)
  }
  class DefaultTextAreaWithNextFocus(nextComponent: Component) extends DefaultTextArea {
    override def transferFocus(): Unit = {
      nextComponent.requestFocus()
    }
  }
  def withNextFocus(component: Component) = new OutputArea(new DefaultTextAreaWithNextFocus(component))
}

import OutputArea._

class OutputArea(val text: JTextArea) extends javax.swing.JPanel {
  private val scrollPane: JScrollPane =
    new javax.swing.JScrollPane(text,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)

  // when someone prints something that
  // ends in a carriage return, we don't want to print it immediately,
  // because this will make a blank line appear at the bottom of the
  // command center -- so instead we use this flag to delay adding
  // the carriage return until the *next* time something is output!
  private var addCarriageReturn: Boolean = false

  private var lastTemporaryAddition = Option.empty[String]

  // var help: String = null // don't think this is used any longer....
  //
  locally {
    text.setEditable(false)
    text.setDragEnabled(false)
    fontSize(12)
    setLayout(new BorderLayout())
    add(scrollPane, BorderLayout.CENTER)
  }

  def this() = this(new DefaultTextArea())

  def valueText = text.getText

  def fontSize: Int =
    text.getFont.getSize

  def fontSize(fontSize: Int): Unit = {
    text.setFont(new Font(NLogoFonts.platformMonospacedFont, Font.PLAIN, fontSize))
  }

  def clear(): Unit = {
    text.setText("")
    addCarriageReturn = false
  }

  override def getMinimumSize: Dimension =
    new Dimension(MinimumWidth, text.getMinimumSize.height)

  override def getPreferredSize: Dimension =
    new Dimension(PreferredWidth, PreferredHeight)

  override def isFocusable: Boolean = false

  def append(oo: OutputObject, wrapLines: Boolean): Unit = {
    var message = oo.get
    lastTemporaryAddition.foreach { addition =>
      val contents = text.getText
      if (contents.length >= addition.length
          && (contents.substring(contents.length - addition.length) == addition)) {
        text.replaceRange("", contents.length - addition.length, contents.length)
      }
    }
    lastTemporaryAddition = None
    if (wrapLines) {
      val fontMetrics = getFontMetrics(text.getFont)
      val messageLines = LineBreaker.breakLines(message, fontMetrics, text.getWidth - GuessScrollBarWidth)
      val wrappedMessage = new StringBuilder()
      var i = 0
      while (i < messageLines.size) {
        wrappedMessage.append(messageLines.get(i))
        wrappedMessage.append("\n")
        i += 1
      }
      message = wrappedMessage.toString();
    }
    val buf = new StringBuilder();
    if (addCarriageReturn) {
      buf.append('\n')
      addCarriageReturn = false
    }
    buf.append(message);
    if (buf.length > 0 && buf.charAt(buf.length - 1) == '\n') {
      buf.setLength(buf.length - 1)
      addCarriageReturn = true
    }
    text.append(buf.toString)
    lastTemporaryAddition = None
    if (oo.isTemporary) {
      text.select(text.getText.length - buf.length, text.getText.length)
      lastTemporaryAddition = Some(text.getSelectedText)
    }
    // doesn't always work unless we wait til later to do it - ST 8/18/03
    EventQueue.invokeLater(
      new Runnable() {
        override def run(): Unit = {
          scrollPane.getVerticalScrollBar.setValue(scrollPane.getVerticalScrollBar.getMaximum);
          scrollPane.getHorizontalScrollBar.setValue(0)
        }
      })
  }
}
