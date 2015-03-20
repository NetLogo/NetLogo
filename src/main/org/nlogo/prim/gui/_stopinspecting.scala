// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ I18N, Syntax, LogoException }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _stopinspecting extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.AgentType))
  override def perform(context: Context) = {
    val agent = argEvalAgent(context, 0)
    org.nlogo.awt.EventQueue.invokeLater(
      new Runnable {
        override def run() {
          workspace.stopInspectingAgent(agent)
        }})
    context.ip = next
  }
}

class _stopinspectingdeadagents extends Command {
  override def syntax = Syntax.commandSyntax
  override def perform(context: Context) {
    org.nlogo.awt.EventQueue.invokeLater(
      new Runnable {
        override def run() {
          workspace.stopInspectingDeadAgents()
        }})
    context.ip = next
  }
}
