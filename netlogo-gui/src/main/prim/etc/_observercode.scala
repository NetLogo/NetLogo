// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled }

class _observercode extends Command with CustomAssembled {
  override def syntax =
    Syntax.commandSyntax("O---", false)
  override def perform(context: Context) {
    throw new UnsupportedOperationException
  }
  override def assemble(a: AssemblerAssistant) {
    // by doing nothing here, drop out of existence
  }
}
