// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener }
import javax.swing.{ AbstractAction, Action, JCheckBox, JPanel }

import org.nlogo.core.I18N, I18N.Prefix
import org.nlogo.swing.{ Button, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.LoadEndEvent

class ViewUpdatePanel(workspace: GUIWorkspace, speedSlider: SpeedSliderPanel, displaySwitch: JCheckBox,
                      tickCounter: TickCounterLabel)
  extends JPanel(new GridBagLayout) with Transparent with LoadEndEvent.Handler with ThemeSync {

  implicit val prefix = Prefix("tabs.run")

  private val updateModeChooser = new UpdateModeChooser(workspace)

  private val settingsButton = new SettingsButton(new EditSettings(workspace.viewWidget.settings))

  displaySwitch.addItemListener(new ViewUpdateListener(speedSlider))

  updateModeChooser.refreshSelection()

  locally {
    val c = new GridBagConstraints

    c.gridy = 0
    c.insets = new Insets(6, 0, 3, 12)

    add(displaySwitch, c)

    c.gridy = 1
    c.fill = GridBagConstraints.HORIZONTAL
    c.insets = new Insets(0, 0, 6, 12)

    add(updateModeChooser, c)

    c.fill = GridBagConstraints.NONE
    c.insets = new Insets(0, 0, 6, 6)

    add(settingsButton, c)
  }

  override def addNotify(): Unit = {
    super.addNotify()
    getComponents.foreach(_.setFocusable(false))
  }

  def handle(e: LoadEndEvent): Unit = {
    updateModeChooser.refreshSelection()
    speedSlider.setValue(workspace.speedSliderPosition.toInt)
  }

  def syncTheme() {
    displaySwitch.setForeground(InterfaceColors.TOOLBAR_TEXT)

    speedSlider.syncTheme()
    tickCounter.syncTheme()
    updateModeChooser.syncTheme()
    settingsButton.syncTheme()
  }

  private class ViewUpdateListener(slider: SpeedSliderPanel) extends ItemListener {
    private var speed = 0

    def itemStateChanged(e: ItemEvent): Unit = {
      val selected = e.getStateChange == ItemEvent.SELECTED
      if (selected != speedSlider.isEnabled) {
        slider.setEnabled(selected)
        if (selected)
          slider.setValue(speed)
        else {
          speed = slider.getValue
          slider.setValue(speedSlider.getMaximum)
        }
      }
    }
  }

  private class SettingsButton(action: Action) extends Button(action) {
    override def getPreferredSize: Dimension =
      new Dimension(super.getPreferredSize.width, updateModeChooser.getPreferredSize.height)
  }

  private class EditSettings(settings: WorldViewSettings)
    extends AbstractAction(I18N.gui("settingsButton")) {
    putValue(Action.SHORT_DESCRIPTION, I18N.gui("settingsButton.tooltip"))
    def actionPerformed(e: ActionEvent) {
      new Events.EditWidgetEvent(settings).raise(e.getSource)
    }
  }
}
