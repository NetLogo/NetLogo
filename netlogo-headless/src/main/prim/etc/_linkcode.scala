// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled }

class _linkcode extends Command with CustomAssembled {
  override def perform(context: Context) {
    throw new UnsupportedOperationException
  }
  override def assemble(a: AssemblerAssistant) {
    // by doing nothing here, drop out of existence
  }
}
