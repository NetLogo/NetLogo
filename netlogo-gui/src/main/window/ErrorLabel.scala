// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Cursor, Dimension }
import javax.swing.{ JLabel, JTextPane }

import org.nlogo.swing.{ BoxAlign, BoxRow, MaximumHeight, Utils, Zoomable, ZoomableBorder }
import org.nlogo.theme.InterfaceColors

class ErrorLabel extends BoxRow(6, BoxAlign.Start) with MaximumHeight {
  private val icon = new JLabel
  private val label = new JTextPane with Zoomable {
    setEditable(false)
    setOpaque(false)
    setBorder(null)
    setContentType("text/html")
    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))
    setCaretColor(InterfaceColors.Transparent)

    override def getMaximumSize: Dimension =
      new Dimension(ErrorLabel.this.getWidth, super.getMaximumSize.height)
  }

  setOpaque(true)
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(icon)
  add(label)

  setVisible(false)

  def setText(text: String): Unit = {
    label.setText(text)
  }

  def setError(error: Option[Exception], offset: Int, respectFocus: Boolean = true): Unit = {
    error match {
      case Some(e) =>
        label.setForeground(InterfaceColors.errorLabelText())
        setBackground(InterfaceColors.errorLabelBackground())
        icon.setIcon(Utils.iconScaledWithColor("/images/error.png", 15, 15, () => InterfaceColors.errorLabelText()))
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
                     () => InterfaceColors.warningLabelText()))
        label.setText(s"<html><b>${encodeHTML(str)}</b></html>")
        setVisible(true)

      case _ =>
        setVisible(false)
    }
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
