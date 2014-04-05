// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context, CustomAssembled, AssemblerAssistant }

class _patchcode extends Command with CustomAssembled {
  override def syntax =
    SyntaxJ.commandSyntax("--P-", false)
  override def perform(context: Context) {
    throw new UnsupportedOperationException
  }
  override def assemble(a: AssemblerAssistant) {
    // by doing nothing here, drop out of existence
  }
}
