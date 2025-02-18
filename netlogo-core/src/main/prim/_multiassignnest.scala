// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }

class _multiassignnest(totalNeeded: Int) extends Command {
  override def perform(context: Context): Unit = {
    try {
      MultiAssign.nest(context.job.workspace, totalNeeded)
    } catch {
      case NestException(list) =>
        throw new RuntimePrimitiveException(context, this, s"Expected list of length ${totalNeeded}, got ${list}")
    }

    context.ip = next
  }
}
