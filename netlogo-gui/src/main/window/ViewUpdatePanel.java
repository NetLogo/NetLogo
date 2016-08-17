// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.I18N;
import org.nlogo.core.UpdateMode;
import org.nlogo.core.UpdateModeJ;

public strictfp class ViewUpdatePanel
    extends javax.swing.JPanel
    implements
    org.nlogo.window.Events.LoadEndEvent.Handler {
  private final GUIWorkspace workspace;

  private final SpeedSliderPanel speedSlider;

  private final javax.swing.JComboBox<String> viewUpdates = new javax.swing.JComboBox<String>();

  private final WorldViewSettings settings;

  private int speed;

  public ViewUpdatePanel(GUIWorkspace workspace, final javax.swing.JCheckBox displaySwitch, boolean editable, TickCounterLabel tickCounter) {
    this.workspace = workspace;
    speedSlider = new SpeedSliderPanel(workspace, tickCounter);
    displaySwitch.addChangeListener
        (new javax.swing.event.ChangeListener() {
          public void stateChanged(javax.swing.event.ChangeEvent e) {
            boolean selected = displaySwitch.isSelected();
            if (selected != speedSlider.isEnabled()) {
              speedSlider.setEnabled(selected);
              if (selected) {
                speedSlider.setValue(speed);
              } else {
                speed = speedSlider.getValue();
                speedSlider.setValue(speedSlider.getMaximum());
              }
            }
          }
        });
    org.nlogo.awt.Fonts.adjustDefaultFont(displaySwitch);
    settings = workspace.viewWidget.settings();
    add(speedSlider);
    SettingsButton settingsButton = new SettingsButton();
    viewUpdates.addItem(I18N.guiJ().get("tabs.run.viewUpdates.dropdown.onticks"));
    viewUpdates.addItem(I18N.guiJ().get("tabs.run.viewUpdates.dropdown.continuous"));
    viewUpdates.setToolTipText(I18N.guiJ().get("tabs.run.viewUpdates.dropdown.tooltip"));
    // we don't want a settings button in the applet ev 2/28/06
    if (editable) {
      javax.swing.JPanel panel = new javax.swing.JPanel
          (new org.nlogo.awt.ColumnLayout
              (0,
                  java.awt.Component.CENTER_ALIGNMENT,
                  java.awt.Component.CENTER_ALIGNMENT));
      panel.add(displaySwitch);
      org.nlogo.awt.Fonts.adjustDefaultFont(viewUpdates);
      panel.setOpaque(false);
      panel.add(viewUpdates);
      add(panel);
      viewUpdates.addItemListener
          (new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent e) {
              setMode((String) e.getItem());
            }
          });
      refreshSelection();
      add(new org.nlogo.swing.ToolBar.Separator());
      add(settingsButton);
    }
    setOpaque(true);
  }

  @Override
  public void setBackground(java.awt.Color color) {
    super.setBackground(color);
    if (speedSlider != null) {
      speedSlider.setBackground(color);
    }
    if (viewUpdates != null) {
      viewUpdates.setBackground(color);
    }
  }

  @Override
  public void addNotify() {
    super.addNotify();
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      comps[i].setFocusable(false);
      org.nlogo.awt.Fonts.adjustDefaultFont(comps[i]);
      if (comps[i] instanceof javax.swing.AbstractButton &&
          // kinda kludgy but we don't want to have the text below
          // the checker in the checkbox in the Code tab ev 8/24/06
          !(comps[i] instanceof javax.swing.JCheckBox)) {
        ((javax.swing.AbstractButton) comps[i])
            .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ((javax.swing.AbstractButton) comps[i])
            .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      }
    }
  }

  private class SettingsButton
      extends javax.swing.JButton {
    public SettingsButton() {
      super(I18N.guiJ().get("tabs.run.settingsButton"));
      setFont
          (new java.awt.Font(org.nlogo.awt.Fonts.platformFont(),
              java.awt.Font.PLAIN, 10));
      setFocusable(false);
      setToolTipText(I18N.guiJ().get("tabs.run.settingsButton.tooltip"));
      addActionListener
          (new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              new org.nlogo.window.Events.EditWidgetEvent(settings)
                  .raise(SettingsButton.this);
            }
          });
    }
  }

  /// methods for keeping menu in sync with reality

  private void refreshSelection() {
    UpdateMode selectedMode = workspace.updateMode();
    if (selectedMode.equals(UpdateModeJ.TICK_BASED())) {
      viewUpdates.setSelectedIndex(0);
    } else if (selectedMode.equals(UpdateModeJ.CONTINUOUS())) {
      viewUpdates.setSelectedIndex(1);
    } else {
      throw new IllegalStateException();
    }
  }

  private void setMode(String name) {
    if (name.equals(I18N.guiJ().get("tabs.run.viewUpdates.dropdown.onticks"))) {
      workspace.updateMode(UpdateModeJ.TICK_BASED());
    } else if (name.equals(I18N.guiJ().get("tabs.run.viewUpdates.dropdown.continuous"))) {
      workspace.updateMode(UpdateModeJ.CONTINUOUS());
    } else {
      throw new IllegalStateException();
    }
  }

  ///

  public void handle(org.nlogo.window.Events.LoadEndEvent e) {
    refreshSelection();
    speedSlider.setValue((int) workspace.speedSliderPosition());
  }

}
