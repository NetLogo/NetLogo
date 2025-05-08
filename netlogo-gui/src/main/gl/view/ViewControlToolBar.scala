// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.{ Font, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, ButtonGroup, JLabel, JPanel }
import javax.swing.border.EmptyBorder

import org.nlogo.api.Perspective
import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, OptionPane, ToolBarToggleButton, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import MouseMotionHandler.{ Mode, OrbitMode, ZoomMode, TranslateMode, InterfaceMode }

class ViewControlToolBar(view: View, inputHandler: MouseMotionHandler)
  extends JPanel(new GridBagLayout) with ThemeSync {

  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("view.3d")

  private val orbitButton = new ModeButton(I18N.gui("orbit"), OrbitMode)
  private val zoomButton = new ModeButton(I18N.gui("zoom"), ZoomMode)
  private val moveButton = new ModeButton(I18N.gui("move"), TranslateMode)
  private val interactButton = new ModeButton(I18N.gui("interact"), InterfaceMode)

  private val resetButton = new Button(I18N.gui("resetPerspective"), view.resetPerspective)
  private val fullScreenButton = new Button(I18N.gui("fullScreen"), () => {
    val options = Seq(I18N.gui.get("common.buttons.continue"), I18N.gui.get("common.buttons.cancel"))
    val isWindows = System.getProperty("os.name").toLowerCase.startsWith("win")

    if (!isWindows || view.viewManager.warned ||
      (new OptionPane(view, I18N.gui.get("common.messages.warning"), I18N.gui("fullScreenWarning"), options,
                      OptionPane.Icons.Warning).getSelectedIndex == 0)) {
      view.viewManager.setFullscreen(true)
      view.viewManager.warned = true
    }
  })

  private val status = new JLabel

  private var perspective: Option[Perspective] = None

  locally {
    val group = new ButtonGroup

    group.add(orbitButton)
    group.add(zoomButton)
    group.add(moveButton)

    val c = new GridBagConstraints

    c.gridy = 0
    c.insets = new Insets(6, 6, 6, 6)

    add(orbitButton, c)

    c.insets = new Insets(6, 0, 6, 6)

    add(zoomButton, c)
    add(moveButton, c)

    if (!view.viewManager.workspace.world.program.dialect.is3D) {
      add(interactButton, c)
      group.add(interactButton)
    }

    add(status)

    c.weightx = 1

    add(new JPanel with Transparent, c)

    c.weightx = 0

    add(resetButton, c)
    add(fullScreenButton, c)

    status.setFont(status.getFont.deriveFont(Font.BOLD))

    orbitButton.doClick()

    setButtonsEnabled(true)
  }

  def setStatus(perspective: Perspective): Unit = {
    // don't update if perspective didn't change
    if (this.perspective.orNull != perspective) {
      this.perspective = Option(perspective)

      status.setText(perspective.toString)

      perspective match {
        case Perspective.Observe =>
          setButtonsEnabled(true)

        case Perspective.Watch(a) =>
          orbitButton.setEnabled(true)
          zoomButton.setEnabled(true)
          moveButton.setEnabled(false)

          if (moveButton.isSelected)
            orbitButton.doClick()

        case Perspective.Ride(a) =>
          setButtonsEnabled(false)
          zoomButton.setEnabled(true)

          if (!interactButton.isSelected && !zoomButton.isSelected)
            zoomButton.doClick()

        case Perspective.Follow(a, _) =>
          setButtonsEnabled(false)
          zoomButton.setEnabled(true)

          if (!interactButton.isSelected && !zoomButton.isSelected)
            zoomButton.doClick()
      }
    }
  }

  private def setButtonsEnabled(enabled: Boolean): Unit = {
    orbitButton.setEnabled(enabled)
    zoomButton.setEnabled(enabled)
    moveButton.setEnabled(enabled)
  }

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.toolbarBackground())

    status.setForeground(InterfaceColors.toolbarText())

    resetButton.syncTheme()
    fullScreenButton.syncTheme()
  }

  private class ModeButton(name: String, mode: Mode)
    extends ToolBarToggleButton(new AbstractAction(name) {
      override def actionPerformed(e: ActionEvent): Unit = {
        inputHandler.setMovementMode(mode)
      }
    }) {
      setBorder(new EmptyBorder(3, 12, 3, 12))
    }
}
