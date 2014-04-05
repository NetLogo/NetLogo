// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled }

class _observercode extends Command with CustomAssembled {
  override def syntax =
    SyntaxJ.commandSyntax("O---", false)
  override def perform(context: Context) {
    throw new UnsupportedOperationException
  }
  override def assemble(a: AssemblerAssistant) {
    // by doing nothing here, drop out of existence
  }
}
