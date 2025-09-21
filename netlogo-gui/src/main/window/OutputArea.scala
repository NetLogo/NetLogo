// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Dimension, EventQueue, Font, Graphics, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JPanel, ScrollPaneConstants }

import org.nlogo.agent.OutputObject
import org.nlogo.awt.{ Fonts => NLogoFonts, LineBreaker }
import org.nlogo.swing.{ RoundedBorderPanel, ScrollPane, TextArea }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

object OutputArea {
  private val PreferredWidth = 200
  private val PreferredHeight = 45
  private val MinimumWidth = 50
  private val GuessScrollBarWidth = 24
  class DefaultTextArea extends TextArea(0, 0, "") {
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

class OutputArea(val text: TextArea) extends JPanel with RoundedBorderPanel with ThemeSync {
  setOpaque(false)

  var zoomFactor = 1.0

  private val scrollPane =
    new ScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
    setBorder(null)
  }

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

  def getTextForExport: String = {
    if (addCarriageReturn) {
      text.getText + "\n"
    } else {
      text.getText
    }
  }

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

  override def paintComponent(g: Graphics): Unit = {
    setDiameter(6 * zoomFactor)

    super.paintComponent(g)
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.commandOutputBackground())
    setBorderColor(InterfaceColors.outputBorder())

    text.syncTheme()

    scrollPane.setBackground(InterfaceColors.commandOutputBackground())
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
    val metrics = getFontMetrics(text.getFont)
    if (wrapLines) {
      message = LineBreaker.breakLines(message, metrics, text.getWidth - GuessScrollBarWidth)
                           .mkString("\n") + "\n"
    }
    val buf = new StringBuilder();
    if (addCarriageReturn) {
      buf.append('\n')
      addCarriageReturn = false
    }
    buf.append(message)
    if (buf.length > 0 && buf.charAt(buf.length - 1) == '\n') {
      buf.setLength(buf.length - 1)
      addCarriageReturn = true
    }
    val cut = {
      val str = buf.toString

      if (metrics.stringWidth(str) > 32767) {
        str.substring(0, 32767 / metrics.charWidth('a') - 3) + "..."
      } else {
        str
      }
    }
    text.append(cut)
    lastTemporaryAddition = None
    if (oo.isTemporary) {
      text.select(text.getText.length - cut.length, text.getText.length)
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
