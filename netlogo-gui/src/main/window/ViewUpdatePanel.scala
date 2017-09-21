// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener }
import javax.swing.{ AbstractAction, Action, JButton, JPanel }

import org.nlogo.awt.{ ColumnLayout, Fonts => NLogoFonts }
import org.nlogo.swing.ToolBar.Separator
import org.nlogo.core.I18N, I18N.Prefix
import org.nlogo.window.Events.LoadEndEvent

class ViewUpdatePanel(workspace: GUIWorkspace, tickCounter: TickCounterLabel)
    extends JPanel with LoadEndEvent.Handler {
  implicit val prefix = Prefix("tabs.run")

  private val displaySwitch     = new DisplaySwitch()
  private val updateModeChooser = new UpdateModeChooser(workspace)
  private val speedSlider       = new SpeedSliderPanel(workspace)
  private val sliderTickPanel   = verticallyStackedPanel(speedSlider, tickCounter)

  private val updateModeControlPanel = {
    val p = verticallyStackedPanel(displaySwitch, updateModeChooser)
    p.setOpaque(false)
    p
  }

  private val settingsButton = new SettingsButton(new EditSettings(workspace.viewWidget.settings))

  displaySwitch.addItemListener(new ViewUpdateListener(speedSlider))
  workspace.registerDisplaySwitch(displaySwitch)

  updateModeChooser.refreshSelection()

  add(sliderTickPanel)
  add(updateModeControlPanel)
  add(new Separator())
  add(settingsButton)
  setOpaque(true)

  override def addNotify(): Unit = {
    super.addNotify()
    getComponents.foreach(_.setFocusable(false))
  }

  def handle(e: LoadEndEvent): Unit = {
    updateModeChooser.refreshSelection()
    speedSlider.setValue(workspace.speedSliderPosition.toInt)
  }

  private def verticallyStackedPanel(components: Component*): JPanel = {
    val p = new JPanel(new ColumnLayout(0, Component.CENTER_ALIGNMENT, Component.CENTER_ALIGNMENT))
    components.foreach(NLogoFonts.adjustDefaultFont)
    components.foreach(p.add)
    p
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
    NLogoFonts.adjustDefaultFont(this)
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
