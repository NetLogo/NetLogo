// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Let
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, MutableLong }

class _wait extends Command with CustomAssembled {

  private[this] val let = Let()

  override def perform(context: Context) {
    val now = System.nanoTime()
    val duration = (argEvalDoubleValue(context, 0) * 1000000000.0).toLong
    context.let(let, new MutableLong(now + duration))
    context.ip = next
  }

  override def assemble(a: AssemblerAssistant) {
    a.add(this)
    a.add(new _waitinternal(let))
  }

}
