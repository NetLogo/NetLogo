// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled }

class _loop extends Command with CustomAssembled {
  override def perform(context: Context) {
    // we get custom-assembled out of existence
    throw new IllegalStateException()
  }
  override def assemble(a: AssemblerAssistant) {
    a.comeFrom()
    a.block()
    a.goTo()
  }
}
