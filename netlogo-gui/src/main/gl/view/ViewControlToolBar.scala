// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Box, ButtonGroup, JLabel }

import org.nlogo.api.Perspective
import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxRow, Button, OptionPane, ToolBarToggleButton, Zoomable, ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import MouseMotionHandler.{ Mode, OrbitMode, ZoomMode, TranslateMode, InterfaceMode }

class ViewControlToolBar(view: GLViewInterface, inputHandler: MouseMotionHandler) extends BoxRow(6) with ThemeSync {
  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("view.3d")

  private val orbitButton = new ModeButton(I18N.gui("orbit"), OrbitMode)
  private val zoomButton = new ModeButton(I18N.gui("zoom"), ZoomMode)
  private val moveButton = new ModeButton(I18N.gui("move"), TranslateMode)
  private val interactButton = new ModeButton(I18N.gui("interact"), InterfaceMode)

  private val resetButton = new Button(I18N.gui("resetPerspective"), view.resetPerspective)
  private val fullScreenButton = new Button(I18N.gui("fullScreen"), () => {
    val options = Seq(I18N.gui.get("common.buttons.continue"), I18N.gui.get("common.buttons.cancel"))
    val isWindows = System.getProperty("os.name").toLowerCase.startsWith("win")

    if (!isWindows || view.warned ||
      (new OptionPane(view, I18N.gui.get("common.messages.warning"), I18N.gui("fullScreenWarning"), options,
                      OptionPane.Icons.Warning).getSelectedIndex == 0)) {
      view.setFullscreen(true)
      view.warned = true
    }
  })

  private val status = new JLabel with Zoomable {
    setBaseFont(getFont.deriveFont(Font.BOLD))
  }

  private var perspective: Option[Perspective] = None

  locally {
    val group = new ButtonGroup

    group.add(orbitButton)
    group.add(zoomButton)
    group.add(moveButton)

    setOpaque(true)
    setBorder(new ZoomableBorder(6, 6, 6, 6))

    add(orbitButton)
    add(zoomButton)
    add(moveButton)

    if (!view.world.program.dialect.is3D) {
      add(interactButton)

      group.add(interactButton)
    }

    add(status)
    add(Box.createHorizontalGlue)
    add(resetButton)
    add(fullScreenButton)

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
      setBorder(new ZoomableBorder(3, 12, 3, 12))
    }
}
