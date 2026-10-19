package org.nlogo.ls.gui

import java.awt.{ Dimension, Graphics, Graphics2D }
import javax.swing.{ BoxLayout, JFrame, JPanel }

import org.nlogo.core.CompilerException
import org.nlogo.headless.HeadlessWorkspace
import org.nlogo.swing.{ ModalProgress, NetLogoIcon }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ AddJobEvent, CompileMoreSourceEvent, CompiledEvent }
import org.nlogo.window.JobWidget

class ViewFrame(ws: HeadlessWorkspace) extends JFrame with CompileMoreSourceEvent.Handler with AddJobEvent.Handler
                                       with ThemeSync with NetLogoIcon with ModalProgress {

  private val viewPanel = new JPanel() {
    override def paintComponent(g: Graphics): Unit = ws.world.synchronized {
      ws.renderer.paint(g.asInstanceOf[Graphics2D], ws)
    }
  }

  val viewContainer = new JPanel
  viewContainer.setLayout(new BoxLayout(viewContainer, BoxLayout.Y_AXIS))

  viewPanel.setPreferredSize(new Dimension((ws.viewWidth * ws.patchSize).toInt, (ws.viewHeight * ws.patchSize).toInt))
  viewPanel.setMinimumSize(viewPanel.getPreferredSize)
  viewPanel.setMaximumSize(viewPanel.getPreferredSize)
  viewContainer.add(viewPanel)

  val panel = new HeadlessPanel(ws, viewContainer)
  getContentPane.add(panel)
  panel.packSplitPane()
  pack()
  panel.resetCommandCenter()

  def handle(e: CompileMoreSourceEvent): Unit = {
    val owner = e.owner
    try {
      val displayName = Some.apply(owner.classDisplayName)
      val results =
        ws.compiler.compileMoreCode(owner.source,
          displayName, ws.world.program,
          ws.procedures, ws.getExtensionManager,
          ws.getLibraryManager, ws.getCompilationEnvironment)
      results.head.init(ws)
      results.head.owner = owner
      new CompiledEvent(owner, ws.world.program, results.head, null).raise(this)
    } catch {
      case error: CompilerException =>
        new CompiledEvent(owner, ws.world.program, null, error).raise(this)
    }
  }

  def handle(e: AddJobEvent): Unit = {
    val agents = if (e.agents == null ) ws.world.agentSetOfKind(e.owner.asInstanceOf[JobWidget].kind) else e.agents
    ws.jobManager.addJob(e.owner, agents, ws, e.procedure)
  }

  def syncTheme(): Unit = {
    viewContainer.setBackground(InterfaceColors.interfaceBackground())

    panel.syncTheme()
  }
}
