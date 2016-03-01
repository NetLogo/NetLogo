// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.I18N;

public strictfp class DisplaySwitch
    extends javax.swing.JCheckBox
    implements java.awt.event.ActionListener {
  private final GUIWorkspace workspace;

  public DisplaySwitch(GUIWorkspace workspace) {
    super(I18N.guiJ().get("tabs.run.viewUpdates.checkbox"));
    this.workspace = workspace;
    setFocusable(false);
    setToolTipText(I18N.guiJ().get("tabs.run.viewUpdates.checkbox.tooltip"));
    addActionListener(this);
  }

  public void actionPerformed(java.awt.event.ActionEvent e) {
    setOn(isSelected());
  }

  public void setOn(boolean on) {
    if (on != isSelected()) {
      setSelected(on);
    }
    if (on) {
      workspace.view.thaw();
      workspace.viewManager.incrementalUpdateFromEventThread();
    } else {
      workspace.view.freeze();
    }
  }
}
