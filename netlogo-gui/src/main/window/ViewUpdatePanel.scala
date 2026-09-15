// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener }
import javax.swing.{ AbstractAction, Action, JCheckBox }

import org.nlogo.core.I18N, I18N.Prefix
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, Button, PreferredSize, ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.LoadEndEvent

class ViewUpdatePanel(workspace: GUIWorkspace, speedSlider: SpeedSliderPanel, displaySwitch: JCheckBox,
                      tickCounter: TickCounterLabel, is3D: Boolean)
  extends BoxColumn(3) with LoadEndEvent.Handler with ThemeSync {

  implicit val prefix: org.nlogo.core.I18N.Prefix = Prefix("tabs.run")

  private val updateModeChooser = new UpdateModeChooser(workspace) {
    override def getPreferredSize: Dimension = {
      new Dimension(displaySwitch.getPreferredSize.width.max(super.getPreferredSize.width),
                    super.getPreferredSize.height)
    }
  }

  private val settingsButton = new SettingsButton(new EditSettings(workspace.viewWidget.settings))

  displaySwitch.addItemListener(new ViewUpdateListener(speedSlider))

  updateModeChooser.refreshSelection()

  setBorder(new ZoomableBorder(6, 0, 6, 6))

  add(new BoxRow(displaySwitch, BoxAlign.Start))
  add(new BoxRow(Seq(updateModeChooser, settingsButton), 12))

  override def addNotify(): Unit = {
    super.addNotify()
    getComponents.foreach(_.setFocusable(false))
  }

  def handle(e: LoadEndEvent): Unit = {
    updateModeChooser.refreshSelection()
    speedSlider.setValue(workspace.speedSliderPosition().toInt)
  }

  override def syncTheme(): Unit = {
    displaySwitch.setForeground(InterfaceColors.toolbarText())

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

  private class SettingsButton(action: Action) extends Button(action) with PreferredSize {
    override def getPreferredSize: Dimension =
      new Dimension(super.getPreferredSize.width, updateModeChooser.getPreferredSize.height)
  }

  private class EditSettings(settings: WorldViewSettings)
    extends AbstractAction(I18N.gui("settingsButton")) {
    putValue(Action.SHORT_DESCRIPTION, I18N.gui("settingsButton.tooltip"))
    def actionPerformed(e: ActionEvent): Unit = {
      if (is3D) {
        new Events.EditView3DEvent(settings).raise(e.getSource)
      } else {
        settings.widgetContainer.foreach(_.editWidget(settings))
      }
    }
  }
}
