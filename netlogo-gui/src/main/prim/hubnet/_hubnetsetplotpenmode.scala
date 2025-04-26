// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet

import org.nlogo.api.CommandRunnable
import org.nlogo.core.PlotPenInterface
import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }

class _hubnetsetplotpenmode extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)
    val mode = argEvalIntValue(context, 1)
    workspace.waitFor(
      new CommandRunnable {
        override def run(): Unit = {
          if (mode < PlotPenInterface.MinMode || mode > PlotPenInterface.MaxMode)
            throw new RuntimePrimitiveException(
              context, _hubnetsetplotpenmode.this,
              mode.toString + " is not a valid plot pen mode (valid modes are 0, 1, and 2)")
          hubNetManager.foreach(_.setPlotPenMode(name, mode))
        }})
    context.ip = next
  }
}
