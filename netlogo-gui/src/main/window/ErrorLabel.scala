// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Cursor, Dimension }
import javax.swing.{ JLabel, JTextPane }

import org.nlogo.swing.{ BoxAlign, BoxRow, MaximumHeight, Utils, Zoomable, ZoomableBorder }
import org.nlogo.theme.InterfaceColors

class ErrorLabel extends BoxRow(6, BoxAlign.Start) with MaximumHeight with Zoomable {
  private val icon = new JLabel
  private val label = new JTextPane {
    setEditable(false)
    setOpaque(false)
    setBorder(null)
    setContentType("text/html")
    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))
    setCaretColor(InterfaceColors.Transparent)

    override def getMaximumSize: Dimension =
      new Dimension(ErrorLabel.this.getWidth, super.getMaximumSize.height)
  }

  private var currentIcon: IconType = IconType.Error

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
        currentIcon = IconType.Error

        label.setForeground(InterfaceColors.errorLabelText())
        label.setText(s"<html><b>${encodeHTML(e.getMessage)}</b></html>")

        setBackground(InterfaceColors.errorLabelBackground())
        setIcon()
        setVisible(true)

      case _ =>
        setVisible(false)
    }
  }

  def setWarning(warning: Option[String]): Unit = {
    warning match {
      case Some(str) =>
        currentIcon = IconType.Warning

        label.setForeground(InterfaceColors.warningLabelText())
        label.setText(s"<html><b>${encodeHTML(str)}</b></html>")

        setBackground(InterfaceColors.warningLabelBackground())
        setIcon()
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

  private def setIcon(): Unit = {
    val size: Int = Utils.zoom(15)

    currentIcon match {
      case IconType.Warning =>
        icon.setIcon(Utils.iconScaledWithColor("/images/exclamation-triangle.png", size, size,
                     InterfaceColors.warningLabelText()))

      case IconType.Error =>
        icon.setIcon(Utils.iconScaledWithColor("/images/error.png", size, size, InterfaceColors.errorLabelText()))
    }
  }

  override def zoom(oldZoom: Float): Unit = {
    setIcon()
  }

  private sealed abstract trait IconType

  private object IconType {
    case object Warning extends IconType
    case object Error extends IconType
  }
}
