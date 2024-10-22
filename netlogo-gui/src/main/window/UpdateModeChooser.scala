// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Graphics }
import javax.swing.{ JComboBox, JLabel, JList, ListCellRenderer }
import java.awt.event.{ ItemEvent, ItemListener }

import org.nlogo.core.{ I18N, UpdateMode }, I18N.Prefix

object UpdateModeChooser {
  private val Choices = Seq(UpdateMode.TickBased, UpdateMode.Continuous)
}

import UpdateModeChooser._

class UpdateModeChooser(workspace: GUIWorkspace) extends JComboBox[UpdateMode](Choices.toArray[UpdateMode]) with ItemListener {
  implicit val prefix = Prefix("tabs.run.viewUpdates")
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

  override def paintComponent(g: Graphics) {
    setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    setForeground(InterfaceColors.TOOLBAR_TEXT)

    super.paintComponent(g)
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
