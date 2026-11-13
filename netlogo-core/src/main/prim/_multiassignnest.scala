// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Dump
import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }

class _multiassignnest(name: String, totalNeeded: Int) extends Command {
  override def perform(context: Context): Unit = {
    try {
      MultiAssign.nest(context.job.workspace, totalNeeded)
    } catch {
      case NestException(list) =>
        val message = loc"""|The list of values for $name must be at least as long as the list of names.  We need
                            |$totalNeeded value(s) but only got ${list.size} from the list ${Dump.logoObject(list)}."""

        throw new RuntimePrimitiveException(context, this, message)
    }

    context.ip = next
  }
}
