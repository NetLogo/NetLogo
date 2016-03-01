// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import org.nlogo.api.{ Agent, AgentFollowingPerspective, Perspective }
import org.nlogo.core.I18N
import java.awt.event.{ ActionEvent, ActionListener }
import MouseMotionHandler.{ Mode, OrbitMode, ZoomMode, TranslateMode, InteractMode }

class ViewControlToolBar(view: View, inputHandler: MouseMotionHandler)
    extends javax.swing.JToolBar {

  val orbitAction =
    new MovementAction(I18N.gui.get("view.3d.orbit"), OrbitMode)
  val zoomAction =
    new MovementAction(I18N.gui.get("view.3d.zoom"), ZoomMode)
  val moveAction =
    new MovementAction(I18N.gui.get("view.3d.move"), TranslateMode)
  val interactAction =
    new MovementAction(I18N.gui.get("view.3d.interact"), InteractMode)

  val fullScreenWarning = I18N.gui.get("view.3d.fullScreenWarning")

  val status = new org.nlogo.swing.SelectableJLabel("")
  status.setFont(status.getFont.deriveFont(java.awt.Font.BOLD))

  setFloatable(false)
  val group = new javax.swing.ButtonGroup
  val orbitButton = new javax.swing.JToggleButton(orbitAction)
  add(orbitButton)
  group.add(orbitButton)
  val zoomButton = new javax.swing.JToggleButton(zoomAction)
  add(zoomButton)
  group.add(zoomButton)
  val moveButton = new javax.swing.JToggleButton(moveAction)
  add(moveButton)
  group.add(moveButton)
  val interactButton = new javax.swing.JToggleButton(interactAction)
  if (!view.viewManager.workspace.world.program.dialect.is3D) {
    add(interactButton)
    group.add(interactButton)
  }
  add(javax.swing.Box.createHorizontalStrut(8))
  add(status)
  add(javax.swing.Box.createHorizontalGlue)
  add(javax.swing.Box.createHorizontalStrut(8))
  val resetButton = new javax.swing.JButton(I18N.gui.get("view.3d.resetPerspective"))
  resetButton.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent) {
      view.resetPerspective()
    }
  })
  add(resetButton)
  add(javax.swing.Box.createHorizontalStrut(8))
  val fullScreenButton = new javax.swing.JButton(I18N.gui.get("view.3d.fullScreen"))
  fullScreenButton.addActionListener(
    new ActionListener {
      override def actionPerformed(e: ActionEvent) {
        val options = Array[AnyRef](I18N.gui.get("common.buttons.continue"),
                                    I18N.gui.get("common.buttons.cancel"))
        val isWindows = System.getProperty("os.name").toLowerCase.startsWith("win")
        if (!isWindows || view.viewManager.warned ||
          (0 == org.nlogo.swing.OptionDialog.show(
            view, I18N.gui.get("common.messages.warning"), fullScreenWarning, options))) {
          view.viewManager.setFullscreen(true)
          view.viewManager.warned = true
        }
      }
    })
  add(fullScreenButton)
  add(javax.swing.Box.createHorizontalStrut(16))
  orbitButton.doClick
  add(javax.swing.Box.createHorizontalStrut(8))
  setButtonsEnabled(true)

  private var perspective: Perspective = null

  def setStatus(perspective: Perspective) {
    // don't update if perspective didn't change
    if (this.perspective != perspective) {
      this.perspective = perspective

      perspective match {
        case Perspective.Observe =>
          status.setText("")
          setButtonsEnabled(true)
        case Perspective.Watch(a) =>
          status.setText(I18N.gui.get("view.3d.watching") + " " + a.toString)
          orbitAction.setEnabled(true)
          zoomAction.setEnabled(true)
          moveAction.setEnabled(false)
          if (moveButton.isSelected)
            orbitButton.doClick()
        case Perspective.Ride(a) =>
          status.setText(I18N.gui.get("view.3d.riding") + " " + a.toString)
          setButtonsEnabled(false)
          zoomAction.setEnabled(true)
          if (!interactButton.isSelected && !zoomButton.isSelected)
            zoomButton.doClick()
        case Perspective.Follow(a, _) =>
          status.setText(I18N.gui.get("view.3d.following") + " " + a.toString)
          setButtonsEnabled(false)
          zoomAction.setEnabled(true)
          if (!interactButton.isSelected && !zoomButton.isSelected) {
            zoomButton.doClick()
          }
      }
    }
  }

  private def setButtonsEnabled(enabled: Boolean) {
    orbitAction.setEnabled(enabled)
    zoomAction.setEnabled(enabled)
    moveAction.setEnabled(enabled)
  }

  private def setMovementMode(mode: Mode) {
    inputHandler.setMovementMode(mode)
  }

  class MovementAction(label: String, mode: Mode)
      extends javax.swing.AbstractAction(label) {
    override def actionPerformed(e: ActionEvent) {
      setMovementMode(mode)
    }
  }
}
