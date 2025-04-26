// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Dump
import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }

class _error extends Command {

  override def perform(context: Context): Unit = {
    throw new RuntimePrimitiveException(context, this,
      Dump.logoObject(args(0).report(context)))
  }
}
