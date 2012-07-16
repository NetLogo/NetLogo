// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color.{ WHITE, YELLOW }
import javax.swing.{ BorderFactory, ImageIcon, JLabel, UIManager }
import javax.swing.border.{ EmptyBorder, LineBorder }

class ErrorLabel extends JLabel {

  var compilerError: Option[Exception] = None

  locally {
    setOpaque(true)
    setFont(UIManager.getFont("Label.font"))
    setForeground(UIManager.getColor("Label.foreground"))
    setBackground(YELLOW)
    setIcon(
      new ImageIcon(classOf[ErrorLabel].getResource("/images/stop.gif")))
    setBorder(BorderFactory.createCompoundBorder(
      new LineBorder(WHITE, 4) ,
      new EmptyBorder(4, 24, 4, 4)
    ))
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
    setFont(getFont.deriveFont(
      StrictMath.ceil(originalFontSize * zoomFactor).toFloat))
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
