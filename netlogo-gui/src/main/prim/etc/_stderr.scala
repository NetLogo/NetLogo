// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Dump
import org.nlogo.nvm.{ Command, Context }

class _stderr extends Command {

  override def perform(context: Context): Unit = {
    System.err.println(
      Dump.logoObject(args(0).report(context)))
    context.ip = next
  }
}
