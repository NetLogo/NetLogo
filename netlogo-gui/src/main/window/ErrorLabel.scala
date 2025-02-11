// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

import org.nlogo.swing.Utils
import org.nlogo.theme.InterfaceColors

class ErrorLabel extends JLabel {

  var compilerError: Option[Exception] = None

  locally {
    setOpaque(true)
    setForeground(Color.WHITE)
    setBackground(InterfaceColors.errorLabelBackground)
    setIcon(Utils.icon("/images/error.png"))
    setBorder(new EmptyBorder(6, 6, 6, 6))
    setVisible(compilerError.isDefined)
  }

  def setError(errorOrNull: Exception, offset: Int) {
    compilerError = Option(errorOrNull)
    setVisible(compilerError.isDefined)
    val err = compilerError.map(_.getMessage).getOrElse("")
    setText("<html>" + encodeHTML(err) + "</html>")
  }

  private var originalFontSize = -1

  def zoom(zoomFactor: Double) {
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
