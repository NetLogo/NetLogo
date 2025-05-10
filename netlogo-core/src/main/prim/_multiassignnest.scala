// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Dump
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }

class _multiassignnest(name: String, totalNeeded: Int) extends Command {
  override def perform(context: Context): Unit = {
    try {
      MultiAssign.nest(context.job.workspace, totalNeeded)
    } catch {
      case NestException(list) =>
        val message = I18N.errors.getN("compiler.MultiAssign.tooFewValues", name, totalNeeded.toString, list.size.toString, Dump.logoObject(list))
        throw new RuntimePrimitiveException(context, this, message)
    }

    context.ip = next
  }
}
