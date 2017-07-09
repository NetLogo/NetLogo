// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ AnonymousProcedure, Command, Context, RuntimePrimitiveException, NonLocalExit }

class _apply extends Command {
  override def perform(context: Context) = {
    val cmd = argEvalAnonymousCommand(context, 0)
    val list = argEvalList(context, 1)
    if (list.size < cmd.syntax.minimum)
      throw new RuntimePrimitiveException(context, this, AnonymousProcedure.missingInputs(cmd, list.size))
    try {
      cmd.perform(context, list.toVector.toArray)
      context.ip = next
    } catch {
      case _: NonLocalExit if ! context.activation.procedure.isReporter =>
        context.stop()
    }
  }
}
