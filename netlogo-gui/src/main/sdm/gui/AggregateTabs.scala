// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ Color, Component }
import java.awt.event.{ ComponentEvent, ComponentListener }
import javax.swing.{ JTabbedPane, SwingConstants }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import org.nlogo.core.{ CompilerException, I18N }

object AggregateTabs {
  private val ErrorColor: Color = Color.RED
}

import AggregateTabs._

class AggregateTabs(editor: AggregateModelEditor, editorTab: AggregateEditorTab, val proceduresTab: AggregateProceduresTab)
  extends JTabbedPane(SwingConstants.TOP) with ChangeListener {

  addChangeListener(this)

  add(I18N.gui.get("tools.sdm.diagram"), editorTab)
  add(I18N.gui.get("tabs.code"), proceduresTab)
  setSelectedComponent(editorTab)

  addComponentListener(new ComponentListener {
    def componentHidden(e: ComponentEvent): Unit = {
      resizeEditorPane()
    }
    def componentMoved(e: ComponentEvent): Unit = {}

    def componentResized(e: ComponentEvent): Unit = {
      resizeEditorPane()
    }
    def componentShown(e: ComponentEvent): Unit = {
      resizeEditorPane()
    }
    private def resizeEditorPane(): Unit = {
      proceduresTab.resizeTo(getSize)
    }
  })

  override def stateChanged(e: ChangeEvent): Unit = {
    proceduresTab.setText(editor.toNetLogoCode)
  }

  private[gui] def recolorTab(component: Component, hasError: Boolean) {
    val index = indexOfComponent(component)
    setForegroundAt(index, if (hasError) ErrorColor else null)
  }

  private[gui] def setError(e: CompilerException, offset: Int) {
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
}
