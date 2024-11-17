// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ Color, Component, Graphics }
import javax.swing.{ JComponent, JTabbedPane, SwingConstants }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.plaf.basic.BasicTabbedPaneUI

import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.swing.{ Utils => SwingUtils }

object AggregateTabs {
  private val ErrorColor: Color = Color.RED
}

import AggregateTabs._

class AggregateTabs(editor: AggregateModelEditor, editorTab: AggregateEditorTab, val proceduresTab: AggregateProceduresTab)
  extends JTabbedPane(SwingConstants.TOP) with ChangeListener with ThemeSync {
  
  private class TabsPanelUI extends BasicTabbedPaneUI with ThemeSync {
    override def getTabLabelShiftY(tabPlacement: Int, tabIndex: Int, isSelected: Boolean): Int =
      super.getTabLabelShiftY(tabPlacement, tabIndex, true)

    override def paintTabBackground(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                                    isSelected: Boolean) {
      val g2d = SwingUtils.initGraphics2D(g)

      if (isSelected)
        g2d.setColor(InterfaceColors.TAB_BACKGROUND_SELECTED)
      else
        g2d.setColor(InterfaceColors.TAB_BACKGROUND)
      
      g2d.fillRect(x, y, w, h)
    }

    override def paintTabBorder(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int,
                                isSelected: Boolean) {
      // no tab border
    }

    override def paintContentBorder(g: Graphics, tabPlacement: Int, selectedIndex: Int) {
      // no content border
    }

    override def paint(g: Graphics, c: JComponent) {
      val g2d = SwingUtils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.TOOLBAR_BACKGROUND)
      g2d.fillRect(0, 0, getWidth, getHeight)

      super.paint(g, c)
    }

    def syncTheme() {
      getComponents.foreach(_ match {
        case ts: ThemeSync => ts.syncTheme()
        case _ =>
      })
    }
  }

  private val tabsUI = new TabsPanelUI

  setUI(tabsUI)
  setFocusable(false)

  addChangeListener(this)

  add(I18N.gui.get("tools.sdm.diagram"), editorTab)
  add(I18N.gui.get("tabs.code"), proceduresTab)

  setSelectedComponent(editorTab)

  override def stateChanged(e: ChangeEvent): Unit = {
    proceduresTab.setText(editor.toNetLogoCode)
  }

  private[gui] def recolorTab(component: Component, hasError: Boolean) {
    val index = indexOfComponent(component)
    setForegroundAt(index, if (hasError) ErrorColor else InterfaceColors.TOOLBAR_TEXT)
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

  def syncTheme() {
    tabsUI.syncTheme()

    setForeground(InterfaceColors.TOOLBAR_TEXT)
  }
}
