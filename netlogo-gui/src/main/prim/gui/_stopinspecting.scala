// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.nvm.{ Command, Context }

class _stopinspecting extends Command {

  override def perform(context: Context) = {
    val agent = argEvalAgent(context, 0)
    org.nlogo.awt.EventQueue.invokeLater(
      new Runnable {
        override def run(): Unit = {
          workspace.stopInspectingAgent(agent)
        }})
    context.ip = next
  }
}

class _stopinspectingdeadagents extends Command {

  override def perform(context: Context): Unit = {
    org.nlogo.awt.EventQueue.invokeLater(
      new Runnable {
        override def run(): Unit = {
          workspace.stopInspectingDeadAgents()
        }})
    context.ip = next
  }
}
