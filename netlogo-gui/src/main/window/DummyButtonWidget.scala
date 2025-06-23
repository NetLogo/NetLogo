// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import javax.swing.JLabel

import org.nlogo.core.{ AgentKind, I18N, Button => CoreButton, Widget => CoreWidget }
import org.nlogo.theme.InterfaceColors

object DummyButtonWidget {
  private val MinimumWidth = 55
  private val MinimumHeight = 33
}

class DummyButtonWidget extends SingleErrorWidget with Editable {
  import DummyButtonWidget._

  private var _actionKey: Char = '\u0000'
  private var _keyEnabled: Boolean = false
  private var _name: String = ""

  private val nameLabel = new JLabel
  private val keyLabel = new JLabel

  keyLabel.setFont(keyLabel.getFont.deriveFont(11.0f))

  setLayout(null)

  add(nameLabel)
  add(keyLabel)

  override def editPanel: EditPanel = new DummyButtonEditPanel(this)

  override def getEditable: Option[Editable] = Some(this)

  def actionKey: Char = _actionKey

  def setActionKey(actionKey: Char): Unit = {
    _actionKey = actionKey
    keyLabel.setText(actionKeyString)
    repaint()
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
    nameLabel.setText(displayName)
    repaint()
  }

  /// sizing

  override def getMinimumSize: Dimension =
    new Dimension(MinimumWidth, MinimumHeight)

  override def getPreferredSize: Dimension =
    new Dimension(MinimumWidth.max(super.getPreferredSize.width), MinimumHeight.max(super.getPreferredSize.height))

  override def doLayout(): Unit = {
    val nameSize = nameLabel.getPreferredSize
    val keySize = keyLabel.getPreferredSize

    nameLabel.setBounds(getWidth / 2 - nameSize.width / 2, getHeight / 2 - nameSize.height / 2, nameSize.width,
                        nameSize.height)
    keyLabel.setBounds(getWidth - keySize.width - 4, 2, keySize.width, keySize.height)
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.buttonBackground())

    nameLabel.setForeground(InterfaceColors.buttonText())
    keyLabel.setForeground(InterfaceColors.buttonText())
  }

  ///

  override def model: CoreWidget = {
    val b = getUnzoomedBounds
    val savedActionKey =
      if (actionKey == 0 || actionKey == ' ') None else Some(actionKey)
    CoreButton(
      display    = name.potentiallyEmptyStringToOption,
      x = b.x, y = b.y, width = b.width, height = b.height, oldSize = _oldSize,
      source     = None,               forever    = false,
      buttonKind = AgentKind.Observer, actionKey  = savedActionKey)
  }

  override def load(model: CoreWidget): Unit = {
    model match {
      case button: CoreButton =>
        button.actionKey.foreach(setActionKey(_))
        setDisplayName(button.display.optionToPotentiallyEmptyString)
        oldSize(button.oldSize)
        setSize(button.width, button.height)

      case _ =>
    }
  }
}
