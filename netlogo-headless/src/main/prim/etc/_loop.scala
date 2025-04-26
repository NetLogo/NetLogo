// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ AssemblerAssistant, Command, CompilerScoping, Context, CustomAssembled }

class _loop extends Command with CustomAssembled with CompilerScoping {
  def scopedBlockIndex: Int = 0

  override def perform(context: Context): Unit = {
    // we get custom-assembled out of existence
    throw new IllegalStateException()
  }

  override def assemble(a: AssemblerAssistant): Unit = {
    a.comeFrom()
    a.block()
    a.goTo()
  }
}
