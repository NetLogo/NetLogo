// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener }
import javax.swing.{ AbstractAction, Action, JButton, JCheckBox, JPanel }

import org.nlogo.awt.ColumnLayout
import org.nlogo.core.I18N, I18N.Prefix
import org.nlogo.window.Events.LoadEndEvent

class ViewUpdatePanel(workspace: GUIWorkspace, displaySwitch: JCheckBox, tickCounter: TickCounterLabel)
    extends JPanel with LoadEndEvent.Handler {
  implicit val prefix = Prefix("tabs.run")

  private val updateModeChooser = new UpdateModeChooser(workspace)
  private val speedSlider       = new SpeedSliderPanel(workspace)

  private val settingsButton = new SettingsButton(new EditSettings(workspace.viewWidget.settings))

  displaySwitch.addItemListener(new ViewUpdateListener(speedSlider))

  updateModeChooser.refreshSelection()

  setBackground(InterfaceColors.TOOLBAR_BACKGROUND)

  add(speedSlider)

  locally {
    val panel = new JPanel(new ColumnLayout(0, Component.CENTER_ALIGNMENT, Component.CENTER_ALIGNMENT))

    panel.add(displaySwitch)
    panel.add(updateModeChooser)

    add(panel)
  }

  add(tickCounter)
  add(settingsButton)

  override def addNotify(): Unit = {
    super.addNotify()
    getComponents.foreach(_.setFocusable(false))
  }

  def handle(e: LoadEndEvent): Unit = {
    updateModeChooser.refreshSelection()
    speedSlider.setValue(workspace.speedSliderPosition.toInt)
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

  private class SettingsButton(action: Action) extends JButton(action) {
    setFocusable(false)
  }

  private class EditSettings(settings: WorldViewSettings)
    extends AbstractAction(I18N.gui("settingsButton")) {
    putValue(Action.SHORT_DESCRIPTION, I18N.gui("settingsButton.tooltip"))
    def actionPerformed(e: ActionEvent) {
      new Events.EditWidgetEvent(settings).raise(e.getSource)
    }
  }
}
