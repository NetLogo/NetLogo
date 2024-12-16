// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ Editable, Property }
import org.nlogo.core.{ AgentKind, I18N, Button => CoreButton }
import org.nlogo.awt.{ Fonts => NlogoFonts }

import java.awt.{ Color, Dimension, Font, Graphics }
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

  def propertySet: JList[Property] = Properties.dummyButton

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

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    val savedActionKey =
      if (actionKey == 0 || actionKey == ' ') None else Some(actionKey)
    CoreButton(
      display    = name.potentiallyEmptyStringToOption,
      x = b.x, y = b.y, width = b.width, height = b.height,
      source     = None,               forever    = false,
      buttonKind = AgentKind.Observer, actionKey  = savedActionKey)
  }

  override def load(button: WidgetModel): AnyRef = {
    button.actionKey.foreach(k => actionKey(k))
    name(button.display.optionToPotentiallyEmptyString)
    setSize(button.width, button.height)
    this
  }

}

