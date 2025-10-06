// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Cursor, Dimension }
import javax.swing.{ Box, BoxLayout, JLabel, JPanel, JTextPane }
import javax.swing.border.EmptyBorder

import org.nlogo.swing.Utils
import org.nlogo.theme.InterfaceColors

class ErrorLabel extends JPanel {
  private val icon = new JLabel
  private val label = new JTextPane {
    setEditable(false)
    setOpaque(false)
    setBorder(null)
    setContentType("text/html")
    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))

    override def getMaximumSize: Dimension =
      new Dimension(ErrorLabel.this.getWidth, super.getMaximumSize.height)
  }

  setOpaque(true)
  setBorder(new EmptyBorder(6, 6, 6, 6))
  setVisible(false)
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  add(icon)
  add(Box.createHorizontalStrut(6))
  add(label)
  add(Box.createHorizontalGlue)

  def setText(text: String): Unit = {
    label.setText(text)
  }

  def setError(error: Option[Exception], offset: Int): Unit = {
    error match {
      case Some(e) =>
        label.setForeground(InterfaceColors.errorLabelText())
        setBackground(InterfaceColors.errorLabelBackground())
        icon.setIcon(Utils.iconScaledWithColor("/images/error.png", 15, 15, InterfaceColors.errorLabelText()))
        label.setText(s"<html><b>${encodeHTML(e.getMessage)}</b></html>")
        setVisible(true)

      case _ =>
        setVisible(false)
    }
  }

  def setWarning(warning: Option[String]): Unit = {
    warning match {
      case Some(str) =>
        label.setForeground(InterfaceColors.warningLabelText())
        setBackground(InterfaceColors.warningLabelBackground())
        icon.setIcon(Utils.iconScaledWithColor("/images/exclamation-triangle.png", 15, 15,
                                          InterfaceColors.warningLabelText()))
        label.setText(s"<html><b>${encodeHTML(str)}</b></html>")
        setVisible(true)

      case _ =>
        setVisible(false)
    }
  }

  private var originalFontSize = -1

  def zoom(zoomFactor: Double): Unit = {
    if(originalFontSize == -1)
      originalFontSize = label.getFont.getSize
    label.setFont(label.getFont.deriveFont((originalFontSize * zoomFactor).ceil.toFloat))
    repaint()
    revalidate()
  }

  private def encodeHTML(s: String): String = {
    def encode(c: Char): String =
      if(c > 127 || c=='"' || c=='<' || c=='>' || c=='/')
        "&#"+ c.toInt + ";"
      else
        c.toString
    s.flatMap(encode)
  }

}
