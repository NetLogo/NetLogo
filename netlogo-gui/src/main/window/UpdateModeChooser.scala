// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, JLabel, JPanel, JPopupMenu }

import org.nlogo.core.{ I18N, UpdateMode }
import org.nlogo.swing.{ DropdownArrow, PopupMenuItem, RoundedBorderPanel }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class UpdateModeChooser(workspace: GUIWorkspace) extends JPanel(new GridBagLayout) with RoundedBorderPanel
                                                 with ThemeSync {
  implicit val prefix = I18N.Prefix("tabs.run.viewUpdates")

  setDiameter(6)
  enableHover()

  setToolTipText(I18N.gui("dropdown.tooltip"))
  setFocusable(false)

  private val label = new JLabel(I18N.gui("dropdown.continuous"))
  private val arrow = new DropdownArrow

  locally {
    val c = new GridBagConstraints

    c.weightx = 1
    c.fill = GridBagConstraints.HORIZONTAL
    c.insets = new Insets(4, 6, 4, 6)

    add(label, c)

    c.weightx = 0
    c.fill = GridBagConstraints.NONE

    add(arrow, c)
  }

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      popup.show(UpdateModeChooser.this, 0, getHeight)
    }
  })

  private val popup = new JPopupMenu

  private val continuousAction = new PopupMenuItem(new AbstractAction(I18N.gui("dropdown.continuous")) {
    def actionPerformed(e: ActionEvent) {
      label.setText(I18N.gui("dropdown.continuous"))

      workspace.updateMode(UpdateMode.Continuous)
    }
  })

  val onTicksAction = new PopupMenuItem(new AbstractAction(I18N.gui("dropdown.onticks")) {
    def actionPerformed(e: ActionEvent) {
      label.setText(I18N.gui("dropdown.onticks"))

      workspace.updateMode(UpdateMode.TickBased)
    }
  })

  popup.add(continuousAction)
  popup.add(onTicksAction)

  def refreshSelection(): Unit = {
    workspace.updateMode() match {
      case UpdateMode.Continuous => label.setText(I18N.gui("dropdown.continuous"))
      case UpdateMode.TickBased => label.setText(I18N.gui("dropdown.onticks"))
    }
  }

  def syncTheme() {
    setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
    setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)
    setForeground(InterfaceColors.TOOLBAR_TEXT)

    label.setForeground(InterfaceColors.TOOLBAR_TEXT)

    popup.setBackground(InterfaceColors.MENU_BACKGROUND)

    continuousAction.syncTheme()
    onTicksAction.syncTheme()
  }
}
