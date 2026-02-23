// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ Color, Component }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.theme.InterfaceColors
import org.nlogo.swing.TabbedPane

object AggregateTabs {
  private val ErrorColor: Color = Color.RED
}

import AggregateTabs._

class AggregateTabs(editor: AggregateModelEditor, editorTab: AggregateEditorTab, val proceduresTab: AggregateProceduresTab)
  extends TabbedPane with ChangeListener {

  addChangeListener(this)

  add(I18N.gui.get("tools.sdm.diagram"), editorTab)
  add(I18N.gui.get("tabs.code"), proceduresTab)

  setSelectedComponent(editorTab)

  syncTheme()

  override def stateChanged(e: ChangeEvent): Unit = {
    proceduresTab.setText(editor.toNetLogoCode)
  }

  private[gui] def recolorTab(component: Component, hasError: Boolean): Unit = {
    val index = indexOfComponent(component)
    setForegroundAt(index, if (hasError) ErrorColor else InterfaceColors.toolbarText())
  }

  private[gui] def setError(e: CompilerException, offset: Int): Unit = {
    setSelectedComponent(proceduresTab)
    proceduresTab.setError(e, offset)
    editorTab.setError(e, offset)
    setForegroundAt(indexOfComponent(proceduresTab), ErrorColor)
  }

  private[gui] def clearError(): Unit = {
    editorTab.clearError()
    proceduresTab.clearError()
    setForegroundAt(indexOfComponent(proceduresTab), null)
  }

  override def setTabForegrounds(): Unit = {
    for (i <- 0 until getTabCount) {
      if (getForegroundAt(i) != ErrorColor) {
        if (i == getSelectedIndex)
          setForegroundAt(i, InterfaceColors.tabTextSelected())
        else
          setForegroundAt(i, InterfaceColors.tabText())
      }
    }
  }
}
