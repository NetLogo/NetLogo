// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component
import java.awt.event.{ ItemEvent, ItemListener }
import javax.swing.{ JComboBox, JLabel, JList, ListCellRenderer }
import javax.swing.border.EmptyBorder

import org.nlogo.core.{ I18N, UpdateMode }, I18N.Prefix
import org.nlogo.swing.HoverDecoration

object UpdateModeChooser {
  private val Choices = Seq(UpdateMode.TickBased, UpdateMode.Continuous)
}

import UpdateModeChooser._

class UpdateModeChooser(workspace: GUIWorkspace) extends JComboBox[UpdateMode](Choices.toArray[UpdateMode])
  with ItemListener with RoundedBorderPanel with ThemeSync with HoverDecoration {
  implicit val prefix = Prefix("tabs.run.viewUpdates")

  setBorder(new EmptyBorder(2, 0, 2, 0))
  setDiameter(6)
  enableHover()

  setToolTipText(I18N.gui("dropdown.tooltip"))
  setFocusable(false)
  setRenderer(new UpdateModeRenderer(getRenderer))
  addItemListener(this)

  def itemStateChanged(e: ItemEvent): Unit = {
    e.getItem match {
      case mode: UpdateMode => workspace.updateMode(mode)
      case _                => // ItemEvent.getItem is an AnyRef, but should *always* be an UpdateMode
    }
  }

  def refreshSelection(): Unit = {
    setSelectedItem(workspace.updateMode())
  }

  def syncTheme() {
    setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
    setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)
    setForeground(InterfaceColors.TOOLBAR_TEXT)
  }

  private class UpdateModeRenderer(delegateRenderer: ListCellRenderer[_ >: UpdateMode]) extends ListCellRenderer[UpdateMode] {
    def getListCellRendererComponent(list: JList[_ <: UpdateMode], value: UpdateMode, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
      (delegateRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus), value) match {
        case (label: JLabel, UpdateMode.Continuous) =>
          label.setText(I18N.gui("dropdown.continuous"))
          label
        case (label: JLabel, UpdateMode.TickBased) =>
          label.setText(I18N.gui("dropdown.onticks"))
          label
        case (cell, _) => cell
      }
    }
  }
}
