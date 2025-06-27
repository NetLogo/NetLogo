// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel
import javax.swing.border.EmptyBorder

import org.nlogo.swing.Utils
import org.nlogo.theme.InterfaceColors

class ErrorLabel extends JLabel {
  setOpaque(true)
  setBorder(new EmptyBorder(6, 6, 6, 6))
  setVisible(false)

  def setError(error: Option[Exception], offset: Int): Unit = {
    error match {
      case Some(e) =>
        setForeground(InterfaceColors.errorLabelText())
        setBackground(InterfaceColors.errorLabelBackground())
        setIcon(Utils.iconScaledWithColor("/images/error.png", 15, 15, InterfaceColors.errorLabelText()))
        setText(s"<html><b>${encodeHTML(e.getMessage)}</b></html>")
        setVisible(true)

      case _ =>
        setVisible(false)
    }
  }

  def setWarning(warning: Option[String]): Unit = {
    warning match {
      case Some(str) =>
        setForeground(InterfaceColors.warningLabelText())
        setBackground(InterfaceColors.warningLabelBackground())
        setIcon(Utils.iconScaledWithColor("/images/exclamation-triangle.png", 15, 15,
                                          InterfaceColors.warningLabelText()))
        setText(s"<html><b>${encodeHTML(str)}</b></html>")
        setVisible(true)

      case _ =>
        setVisible(false)
    }
  }

  private var originalFontSize = -1

  def zoom(zoomFactor: Double): Unit = {
    if(originalFontSize == -1)
      originalFontSize = getFont.getSize
    setFont(getFont.deriveFont((originalFontSize * zoomFactor).ceil.toFloat))
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
