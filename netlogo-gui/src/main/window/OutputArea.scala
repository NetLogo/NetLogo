// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BasicStroke, Color, Component, Dimension, EventQueue, Font, Graphics, GridBagConstraints,
                  GridBagLayout, Insets }
import javax.swing.{ JPanel, JScrollPane, JTextArea, ScrollPaneConstants }

import org.nlogo.awt.{ Fonts => NLogoFonts, LineBreaker }
import org.nlogo.agent.OutputObject
import org.nlogo.swing.Utils

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

class OutputArea(val text: JTextArea) extends JPanel {
  setBackground(InterfaceColors.TRANSPARENT)

  private val scrollPane: JScrollPane =
    new JScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  
  scrollPane.setBorder(null)
  scrollPane.setBackground(InterfaceColors.TRANSPARENT)

  text.setBackground(InterfaceColors.TRANSPARENT)

  // when someone prints something that
  // ends in a carriage return, we don't want to print it immediately,
  // because this will make a blank line appear at the bottom of the
  // command center -- so instead we use this flag to delay adding
  // the carriage return until the *next* time something is output!
  private var addCarriageReturn: Boolean = false

  private var lastTemporaryAddition = Option.empty[String]

  // var help: String = null // don't think this is used any longer....
  //
  text.setEditable(false)
  text.setDragEnabled(false)
  fontSize(12)
  setLayout(new GridBagLayout)

  locally {
    val c = new GridBagConstraints

    c.weightx = 1
    c.weighty = 1
    c.fill = GridBagConstraints.BOTH
    c.insets = new Insets(3, 3, 3, 3)

    add(scrollPane, c)
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

  override def paintComponent(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)
    g2d.setColor(Color.WHITE)
    g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)
    val stroke = g2d.getStroke
    g2d.setStroke(new BasicStroke(1))
    g2d.setColor(InterfaceColors.OUTPUT_BORDER)
    g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, 6, 6)
    g2d.setStroke(stroke)
    super.paintComponent(g)
  }

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
