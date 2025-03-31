// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Graphics }

import org.nlogo.awt.Fonts
import org.nlogo.core.{ AgentKind, I18N, Button => CoreButton }
import org.nlogo.theme.InterfaceColors

object DummyButtonWidget {
  private val MinimumWidth = 55
  private val MinimumHeight = 33
}

class DummyButtonWidget extends SingleErrorWidget with Editable {
  type WidgetModel = CoreButton

  import DummyButtonWidget._

  private var _actionKey: Char = '\u0000'
  private var _keyEnabled: Boolean = false
  private var _name: String = ""

  override def editPanel: EditPanel = new DummyButtonEditPanel(this)

  def actionKey: Char = _actionKey

  def setActionKey(actionKey: Char): Unit = {
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

  def setDisplayName(name: String): Unit = {
    _name = name
    displayName(name)
  }

  /// sizing

  override def getMinimumSize: Dimension =
    new Dimension(MinimumWidth, MinimumHeight)

  override def getPreferredSize: Dimension =
    new Dimension(MinimumWidth.max(super.getPreferredSize.width), MinimumHeight.max(super.getPreferredSize.height))

  /// painting

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val size = getSize()
    val fontMetrics = g.getFontMetrics
    val labelHeight =
      fontMetrics.getMaxDescent + fontMetrics.getMaxAscent
    val availableWidth = size.width - 8
    val stringWidth = fontMetrics.stringWidth(displayName)
    g.setColor(InterfaceColors.buttonText())

    val shortString =
      Fonts.shortenStringToFit(displayName, availableWidth, fontMetrics)

    val nx =
      if (stringWidth > availableWidth) 4
      else (size.width / 2) - (stringWidth / 2)
    val ny = (size.height / 2) + (labelHeight / 2)
    g.drawString(shortString, nx, ny)

    // now draw keyboard shortcut
    if (actionKeyString != "") {
      val ax = size.width - 4 - fontMetrics.stringWidth(actionKeyString)
      val ay = fontMetrics.getMaxAscent + 2

      g.setColor(
        if (keyEnabled)
          InterfaceColors.buttonText()
        else
          InterfaceColors.buttonTextDisabled()
      )

      g.drawString(actionKeyString, ax - 1, ay)
    }
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.buttonBackground())
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
    button.actionKey.foreach(k => setActionKey(k))
    setDisplayName(button.display.optionToPotentiallyEmptyString)
    setSize(button.width, button.height)
    this
  }

}

