package org.nlogo.gl.view;

import org.nlogo.api.Agent;
import org.nlogo.api.I18N;
import org.nlogo.api.Perspective;

strictfp class ViewControlToolBar
    extends javax.swing.JToolBar {

  private final MouseMotionHandler inputHandler;

  private final javax.swing.JTextField status;
  private final javax.swing.JToggleButton orbitButton;
  private final javax.swing.JToggleButton zoomButton;
  private final javax.swing.JToggleButton moveButton;
  private final javax.swing.JToggleButton interactButton;

  private final javax.swing.AbstractAction ORBIT_ACTION =
      new MovementAction(I18N.gui().get("view.3d.orbit"), View.Mode.ORBIT);
  private final javax.swing.AbstractAction ZOOM_ACTION =
      new MovementAction(I18N.gui().get("view.3d.zoom"), View.Mode.ZOOM);
  private final javax.swing.AbstractAction MOVE_ACTION =
      new MovementAction(I18N.gui().get("view.3d.move"), View.Mode.TRANSLATE);
  private final javax.swing.AbstractAction INTERACT_ACTION =
      new MovementAction(I18N.gui().get("view.3d.interact"), View.Mode.INTERACT);

  private static final String FULLSCREEN_WARNING = I18N.gui().get("view.3d.fullScreenWarning");

  public ViewControlToolBar(final View view, MouseMotionHandler inputHandler) {
    super();

    this.inputHandler = inputHandler;

    setFloatable(false);

    javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();

    orbitButton = new javax.swing.JToggleButton(ORBIT_ACTION);
    add(orbitButton);
    group.add(orbitButton);

    zoomButton = new javax.swing.JToggleButton(ZOOM_ACTION);
    add(zoomButton);
    group.add(zoomButton);

    moveButton = new javax.swing.JToggleButton(MOVE_ACTION);
    add(moveButton);
    group.add(moveButton);

    interactButton = new javax.swing.JToggleButton(INTERACT_ACTION);
    if (!view.viewManager.workspace.world.program().is3D) {
      add(interactButton);
      group.add(interactButton);
    }

    add(javax.swing.Box.createHorizontalStrut(8));

    status = new org.nlogo.swing.SelectableJLabel("");
    status.setFont(status.getFont().deriveFont(java.awt.Font.BOLD));
    add(status);

    add(javax.swing.Box.createHorizontalGlue());
    add(javax.swing.Box.createHorizontalStrut(8));

    final javax.swing.JButton resetButton = new javax.swing.JButton(I18N.gui().get("view.3d.resetPerspective"));
    resetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        view.resetPerspective();
      }
    });
    add(resetButton);

    add(javax.swing.Box.createHorizontalStrut(8));

    final javax.swing.JButton fullscreenButton = new javax.swing.JButton(I18N.gui().get("view.3d.fullScreen"));
    fullscreenButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        String[] options = {I18N.gui().get("common.buttons.continue"), I18N.gui().get("common.buttons.cancel")};
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");

        if (!isWindows || view.viewManager.warned() ||
            (0 == org.nlogo.swing.OptionDialog.show
                (view, I18N.gui().get("common.messages.warning"), FULLSCREEN_WARNING,
                    options))) {
          view.viewManager.setFullscreen(true);
          view.viewManager.warned(true);
        }
      }
    });
    add(fullscreenButton);

    add(javax.swing.Box.createHorizontalStrut(16));

    orbitButton.doClick();


    add(javax.swing.Box.createHorizontalStrut(8));

    setButtonsEnabled(true);
  }

  private Perspective perspective;
  private Agent agent;

  void setStatus(Perspective perspective, Agent agent) {
    // don't update if perspective didn't change
    if (this.perspective != perspective ||
        (agent != null && !agent.equals(this.agent))) {
      this.perspective = perspective;
      this.agent = agent;
      switch (perspective) {
        case OBSERVE:
          status.setText("");
          setButtonsEnabled(true);
          break;

        case WATCH:
          status.setText(I18N.gui().get("view.3d.watching") + agent.toString());
          ORBIT_ACTION.setEnabled(true);
          ZOOM_ACTION.setEnabled(true);
          MOVE_ACTION.setEnabled(false);
          if (moveButton.isSelected()) {
            orbitButton.doClick();
          }
          break;

        case RIDE:
          status.setText(I18N.gui().get("view.3d.riding") + agent.toString());
          setButtonsEnabled(false);
          ZOOM_ACTION.setEnabled(true);
          if ((!interactButton.isSelected()) &&
              (!zoomButton.isSelected())) {
            zoomButton.doClick();
          }
          break;

        case FOLLOW:
          status.setText(I18N.gui().get("view.3d.following") + agent.toString());
          setButtonsEnabled(false);
          ZOOM_ACTION.setEnabled(true);
          if ((!interactButton.isSelected()) &&
              (!zoomButton.isSelected())) {
            zoomButton.doClick();
          }
          break;

        default:
          throw new IllegalStateException();
      }
    }
  }

  private void setButtonsEnabled(boolean enabled) {
    ORBIT_ACTION.setEnabled(enabled);
    ZOOM_ACTION.setEnabled(enabled);
    MOVE_ACTION.setEnabled(enabled);
  }

  private void setMovementMode(View.Mode mode) {
    inputHandler.setMovementMode(mode);
  }

  private final class MovementAction
      extends javax.swing.AbstractAction {
    final View.Mode mode;

    public MovementAction(String label, View.Mode mode) {
      super(label);
      this.mode = mode;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      setMovementMode(mode);
    }
  }
}
