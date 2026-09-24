// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Cursor, Dimension }
import javax.swing.{ Box, JLabel, JTextPane }

import org.nlogo.awt.Hierarchy
import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, Button, MaximumHeight, PreferredSize, Utils, Zoomable,
                         ZoomableBorder }
import org.nlogo.theme.InterfaceColors

class ErrorLabel extends BoxRow(6) with MaximumHeight {
  private var error: Option[Exception] = None

  private val icon = new JLabel
  private val label = new JTextPane with Zoomable with PreferredSize {
    setEditable(false)
    setOpaque(false)
    setBorder(null)
    setContentType("text/html")
    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))
    setCaretColor(InterfaceColors.Transparent)

    override def getPreferredSize: Dimension =
      new Dimension(super.getPreferredSize.width, getFontMetrics(getFont).getHeight)
  }

  val report = new Button(I18N.gui.get("code.error.notHelpful"), () => {
    new ReportNotHelpfulDialog(Hierarchy.getFrame(ErrorLabel.this), error.getOrElse(new Exception))
  }) {
    private def scale(color: Color, factor: Float): Color =
      new Color((color.getRed * factor).toInt, (color.getGreen * factor).toInt, (color.getBlue * factor).toInt)

    override def syncTheme(): Unit = {
      val base: Color = InterfaceColors.errorLabelBackground()

      setBackgroundColor(scale(base, 0.8))
      setBackgroundHoverColor(scale(base, 0.7))
      setBackgroundPressedColor(scale(base, 0.5))
      setBorderColor(InterfaceColors.errorLabelText())
      setForeground(InterfaceColors.errorLabelText())
    }
  }

  setOpaque(true)
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(icon)
  add(new BoxColumn(label, BoxAlign.Center) with PreferredSize)
  add(Box.createHorizontalGlue)
  add(report)

  setVisible(false)

  def setText(text: String): Unit = {
    label.setText(text)
  }

  def setError(error: Option[Exception], offset: Int, respectFocus: Boolean = true): Unit = {
    this.error = error

    error match {
      case Some(e) =>
        label.setForeground(InterfaceColors.errorLabelText())
        setBackground(InterfaceColors.errorLabelBackground())
        icon.setIcon(Utils.iconScaledWithColor(this, "/images/error.png", 15, 15,
                                               () => InterfaceColors.errorLabelText()))
        label.setText(s"<html><b>${encodeHTML(e.getMessage)}</b></html>")
        report.syncTheme()
        report.setVisible(true)
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
        icon.setIcon(Utils.iconScaledWithColor(this, "/images/exclamation-triangle.png", 15, 15,
                     () => InterfaceColors.warningLabelText()))
        label.setText(s"<html><b>${encodeHTML(str)}</b></html>")
        report.setVisible(false)
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
