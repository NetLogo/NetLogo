// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import java.awt.BorderLayout
import javax.swing.JPanel

import org.nlogo.core.Widget
import org.nlogo.swing.SplitPane
import org.nlogo.theme.ThemeSync
import org.nlogo.window.Events

class InterfaceTab(workspace: SemiHeadlessWorkspace)
  extends JPanel(new BorderLayout) with ThemeSync with Events.OutputEvent.Handler {

  private val interfacePanel = new BlockedInterfacePanel(workspace)
  private val outputPanel = new OutputPanel
  private val splitPane = new SplitPane(interfacePanel, outputPanel, None)

  add(splitPane, BorderLayout.CENTER)

  def loadWidget(widget: Widget): Unit = {
    interfacePanel.loadWidget(widget)
  }

  def resetSplitPane(): Unit = {
    splitPane.resetToPreferredSizes()
  }

  override def handle(e: Events.OutputEvent): Unit = {
    if (e.toCommandCenter) {
      if (e.clear)
        outputPanel.clear()

      if (e.outputObject != null)
        outputPanel.append(e.outputObject, e.wrapLines)
    }
  }

  override def syncTheme(): Unit = {
    interfacePanel.syncTheme()
    outputPanel.syncTheme()
  }
}
