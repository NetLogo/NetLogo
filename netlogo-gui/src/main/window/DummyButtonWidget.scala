// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.Editable
import org.nlogo.core.{ I18N, Button => CoreButton }
import org.nlogo.api.Property
import org.nlogo.awt.{ Fonts => NlogoFonts }

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.util.{ List => JList }

object DummyButtonWidget {
  private val MinimumWidth = 55
  private val MinimumHeight = 33
  private val PreferredTextHorizontalPadding = 28
  private val PreferredTextVertialPadding = 12
}

class DummyButtonWidget
  extends SingleErrorWidget
  with Editable {

  type WidgetModel = CoreButton

  import DummyButtonWidget._

  setBackground(InterfaceColors.BUTTON_BACKGROUND)
  setBorder(widgetBorder)
  org.nlogo.awt.Fonts.adjustDefaultFont(this)

  private var _actionKey: Char = '\u0000'
  private var _keyEnabled: Boolean = false
  private var _name: String = ""

  def propertySet: JList[Property] =
    Properties.dummyButton

  def actionKey: Char = _actionKey

  def actionKey(actionKey: Char): Unit = {
    _actionKey = actionKey
  }

  private def actionKeyString: String =
    _actionKey match {
      case '\u0000' => ""
      case k        => k.toString
    }

  def keyEnabled: Boolean = _keyEnabled

  def keyEnabled(keyEnabled: Boolean): Unit = {
    if (_keyEnabled != keyEnabled) {
      _keyEnabled = keyEnabled
      repaint()
    }
  }

  /// editability

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.button")

  def name: String = _name

  def name(name: String): Unit = {
    _name = name
    displayName(name)
  }

  /// sizing

  override def getMinimumSize: Dimension =
    new Dimension(MinimumWidth, MinimumHeight)

  override def getPreferredSize(font: Font): Dimension = {
    val size = getMinimumSize
    val fontMetrics = getFontMetrics(font)
    size.width = StrictMath.max(size.width,
      fontMetrics.stringWidth(displayName) +
      PreferredTextHorizontalPadding)
    size.height = StrictMath.max(size.height,
      fontMetrics.getMaxDescent +
      fontMetrics.getMaxAscent +
      PreferredTextVertialPadding)
    size
  }

  /// painting

  override def paintComponent(g: Graphics): Unit = {
    g.setColor(getBackground)
    g.fillRect(0, 0, getWidth, getHeight)
    val size = getSize()
    val fontMetrics = g.getFontMetrics
    val labelHeight =
      fontMetrics.getMaxDescent + fontMetrics.getMaxAscent
    val availableWidth = size.width - 8
    val stringWidth = fontMetrics.stringWidth(displayName)
    g.setColor(getForeground)

    val shortString =
      NlogoFonts.shortenStringToFit(displayName, availableWidth, fontMetrics)

    val nx =
      if (stringWidth > availableWidth) 4
      else (size.width / 2) - (stringWidth / 2)
    val ny = (size.height / 2) + (labelHeight / 2)
    g.drawString(shortString, nx, ny)

    // now draw keyboard shortcut
    if (actionKeyString != "") {
      val ax = size.width - 4 - fontMetrics.stringWidth(actionKeyString)
      val ay = fontMetrics.getMaxAscent + 2
      val keyColor =
        if (keyEnabled) Color.BLACK else Color.GRAY

      g.setColor(keyColor)
      g.drawString(actionKeyString, ax - 1, ay)
    }
  }

  ///

  override def save: String = {
    val s = new StringBuilder
    s.append("BUTTON\n")
    s.append(getBoundsString)
    if (!name.trim.equals(""))
      s.append(name + "\n")
    else
      s.append("NIL\n")
    s.append("NIL\n")
    s.append("NIL\n")
    s.append(1 + "\n") // for compatability
    s.append("T\n")  // show display name

    val temp = "OBSERVER\n" // assume Observer button
    s.append(temp)
    s.append("NIL\n")
    if (actionKey == 0 || actionKey == ' ')
      s.append("NIL\n")
    else
      s.append(actionKey + "\n")
    return s.toString
  }

  override def load(button: WidgetModel, helper: Widget.LoadHelper): AnyRef = {
    button.actionKey.foreach(k => actionKey(k))

    val normalizedName =
      button.display.map(n => if (n == "NIL") "" else n).getOrElse("")
    name(normalizedName)

    setSize(button.right - button.left, button.top - button.bottom)
    this
  }

}

