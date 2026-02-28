// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import java.awt.{ Component, Dimension }
import java.awt.event.MouseAdapter
import javax.swing.{ JLayeredPane, JPanel }

import org.nlogo.plot.PlotManager
import org.nlogo.swing.Transparent
import org.nlogo.window.{ DefaultEditorFactory, Event, InterfacePanelLite }

class BlockedInterfacePanel(frame: BehaviorSpaceFrame, workspace: SemiHeadlessWorkspace)
  extends InterfacePanelLite(workspace.viewWidget, workspace, workspace,
                             new PlotManager(workspace, workspace.world.mainRNG.clone),
                             new DefaultEditorFactory(workspace), workspace.extensionManager) with Event.LinkChild {

  private val interceptPanel = new JPanel with Transparent {
    setFocusable(true)
    setLayout(null)

    addMouseListener(new MouseAdapter {})

    override def doLayout(): Unit = {
      setSize(getPreferredSize)
    }

    override def getPreferredSize: Dimension =
      getParent.getSize

    override def getMinimumSize: Dimension =
      getPreferredSize

    override def getMaximumSize: Dimension =
      getPreferredSize
  }

  add(interceptPanel, JLayeredPane.DRAG_LAYER)

  override def getLinkParent: Component =
    frame

  override def doLayout(): Unit = {
    super.doLayout()

    interceptPanel.revalidate()
  }
}
