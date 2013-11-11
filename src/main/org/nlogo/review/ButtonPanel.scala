// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.window.PaintableButton
import org.nlogo.window.ButtonWidget.ButtonType

class ButtonPanel(
  override val panelBounds: java.awt.Rectangle,
  override val originalFont: java.awt.Font,
  override val actionKeyString: String,
  override val buttonType: ButtonType,
  override val displayName: String,
  override val forever: Boolean)
  extends WidgetPanel
  with PaintableButton {

  override val buttonUp: Boolean = true
  override val disabledWaitingForSetup: Boolean = true // to appear non-pressable
  override val error: Exception = null
  override val keyEnabled: Boolean = false // to appear non-pressable
  override val running: Boolean = false

}