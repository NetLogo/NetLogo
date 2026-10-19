package view25d.view

import java.awt.{ Dimension, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ ButtonGroup, JPanel }

import org.nlogo.swing.{ Button, RadioButton }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class VarviewDashboard(window: VarviewWindow) extends JPanel(new GridBagLayout) with ThemeSync {
  private val orbitButton = new RadioButton("orbit", () => window.setMode(VarviewWindow.ORBIT)) {
    setSelected(true)
  }

  private val zoomButton = new RadioButton("zoom", () => window.setMode(VarviewWindow.ZOOM))
  private val moveButton = new RadioButton("move", () => window.setMode(VarviewWindow.MOVE))
  private val zscaleButton = new RadioButton("z-scale", () => window.setMode(VarviewWindow.SCALEZ))

  private val resetButton = new Button("reset perspective", () => {
    window.setMode(VarviewWindow.ORBIT)
    // window.zScale = 1
    window.resetPerspective()

    orbitButton.setSelected(true)
  })

  locally {
    val group = new ButtonGroup

    group.add(orbitButton)
    group.add(zoomButton)
    group.add(moveButton)
    group.add(zscaleButton)

    val c = new GridBagConstraints

    c.gridy = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    add(orbitButton, c)

    c.insets = new Insets(6, 0, 6, 6)

    add(zoomButton, c)
    add(moveButton, c)
    add(zscaleButton, c)

    c.anchor = GridBagConstraints.EAST
    c.weightx = 1

    add(resetButton, c)
  }

  setSize(new Dimension(600, 40))

  def syncTheme(): Unit = {
    setBackground(InterfaceColors.toolbarBackground())

    orbitButton.syncTheme()
    zoomButton.syncTheme()
    moveButton.syncTheme()
    zscaleButton.syncTheme()
    resetButton.syncTheme()
  }
}
