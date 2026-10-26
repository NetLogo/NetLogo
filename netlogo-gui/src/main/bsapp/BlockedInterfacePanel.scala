// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import java.awt.{ Component, Dimension }
import java.awt.event.MouseAdapter
import javax.swing.{ JLayeredPane, JPanel }

import org.nlogo.swing.{ PreferredSize, Transparent }
import org.nlogo.window.{ DefaultEditorFactory, DummyConstraintManager, Event, InterfacePanelLite }

class BlockedInterfacePanel(frame: BehaviorSpaceFrame, workspace: SemiHeadlessWorkspace)
  extends InterfacePanelLite(workspace.viewWidget, new DummyConstraintManager, workspace, workspace,
                             workspace.plotManager, new DefaultEditorFactory(workspace), workspace.extensionManager,
                             workspace.compilerManager)
    with Event.LinkChild {

  private val interceptPanel = new JPanel with PreferredSize with Transparent {
    setFocusable(true)
    setLayout(null)

    addMouseListener(new MouseAdapter {})

    override def doLayout(): Unit = {
      setSize(getPreferredSize)
    }

    override def getPreferredSize: Dimension =
      getParent.getSize
  }

  add(interceptPanel, JLayeredPane.DRAG_LAYER)

  override def getLinkParent: Component =
    frame

  override def doLayout(): Unit = {
    super.doLayout()

    interceptPanel.revalidate()
  }
}
