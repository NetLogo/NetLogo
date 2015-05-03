// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Font }
import java.awt.event.{ ActionEvent, ActionListener, ItemEvent, ItemListener }
import javax.swing.{ AbstractButton, JButton, JCheckBox, JComboBox, JPanel, SwingConstants }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import org.nlogo.api.{ I18N, UpdateMode }
import org.nlogo.awt.{ ColumnLayout, Fonts }
import org.nlogo.swing.ToolBar

class ViewUpdatePanel(workspace: GUIWorkspace, displaySwitch: JCheckBox, editable: Boolean)
    extends JPanel with Events.LoadEndEventHandler {
  implicit val i18nName = I18N.Prefix("tabs.run.viewUpdates.dropdown")
  private lazy val speedSlider = new SpeedSliderPanel(workspace, true)
  private lazy val viewUpdates = new JComboBox[String]
  private val settings = workspace.viewWidget.settings
  private val settingsButton = new SettingsButton
  private var speed = speedSlider.getValue
  
  displaySwitch.addChangeListener(new ChangeListener {
    def stateChanged(e: ChangeEvent) = {
      val selected = displaySwitch.isSelected
      if(selected != speedSlider.isEnabled) {
        speedSlider.setEnabled(selected)
        if(selected) {
          speedSlider.setValue(speed)
        } else {
          speed = speedSlider.getValue
          speedSlider.setValue(speedSlider.getMaximum)
        }
      }
    }
  })
  Fonts.adjustDefaultFont(displaySwitch)
  add(speedSlider)
  viewUpdates.addItem(I18N.gui("onticks"))
  viewUpdates.addItem(I18N.gui("continuous"))
  viewUpdates.setToolTipText(I18N.gui("tooltip"))
  // we don't want a settings button in the applet ev 2/28/06
  if(editable) {
    val panel = new JPanel(new ColumnLayout(0, Component.CENTER_ALIGNMENT, Component.CENTER_ALIGNMENT))
    panel.add(displaySwitch)
    Fonts.adjustDefaultFont(viewUpdates)
    panel.setOpaque(false)
    panel.add(viewUpdates)
    add(panel)
    viewUpdates.addItemListener(new ItemListener {
      def itemStateChanged(e: ItemEvent) = setMode(e.getItem.toString)
    })
    refreshSelection()
    add(new ToolBar.Separator)
    add(settingsButton)
  }
  setOpaque(true)

  override def setBackground(color: Color) = {
    super.setBackground(color)
    if(speedSlider != null)
      speedSlider.setBackground(color)
    if(viewUpdates != null)
      viewUpdates.setBackground(color)
  }

  override def addNotify() = {
    super.addNotify()
    val comps = getComponents
    comps.foreach { comp =>
      comp.setFocusable(false)
      Fonts.adjustDefaultFont(comp)
      comp match {
        // the `if` is kinda kludgy but we don't want to have the text below
        // the checker in the checkbox in the Code tab ev 8/24/06
        case button: AbstractButton if !button.isInstanceOf[JCheckBox] =>
          button.setVerticalTextPosition(SwingConstants.BOTTOM)
          button.setHorizontalTextPosition(SwingConstants.CENTER)
        case _ => {}
      }
    }
  }
  
  private class SettingsButton extends JButton(I18N.gui.get("tabs.run.settingsButton")) {
    setFont(new Font(Fonts.platformFont, Font.PLAIN, 10))
    setFocusable(false)
    setToolTipText(I18N.gui.get("tabs.run.settingsButton.tooltip"))
    addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) = new Events.EditWidgetEvent(settings).raise(SettingsButton.this)
    })
  }

  /// methods for keeping menu in sync with reality

  private def refreshSelection() =
    if(workspace.updateMode() == UpdateMode.TickBased)
      viewUpdates.setSelectedIndex(0)
    else if(workspace.updateMode() == UpdateMode.Continuous)
      viewUpdates.setSelectedIndex(1)
    else
      throw new IllegalStateException

  private def setMode(name: String) =
    if(name==I18N.gui("onticks"))
      workspace.updateMode(UpdateMode.TickBased)
    else if (name==I18N.gui("continuous"))
      workspace.updateMode(UpdateMode.Continuous)
    else
      throw new IllegalStateException

  ///

  def handle(e: Events.LoadEndEvent) = {
    refreshSelection()
    speedSlider.setValue(workspace.speedSliderPosition.toInt)
  }

}
