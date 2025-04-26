// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled }

class _observercode extends Command with CustomAssembled {

  override def perform(context: Context): Unit = {
    throw new UnsupportedOperationException
  }
  override def assemble(a: AssemblerAssistant): Unit = {
    // by doing nothing here, drop out of existence
  }
}
