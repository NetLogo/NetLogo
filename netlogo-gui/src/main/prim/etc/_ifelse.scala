// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.LogoException
import org.nlogo.nvm.{AssemblerAssistant, Command, Context, CustomAssembled}

final class _ifelse extends Command with CustomAssembled {

  override def toString: String = super.toString + ":+" + offset

  @throws[LogoException]
  override def perform(context: Context): Unit = {
    perform_1(context, argEvalBooleanValue(context, 0))

  }

  def perform_1(context: Context, cond: Boolean): Unit =
    context.ip = if (cond) next else offset

  override def assemble(a: AssemblerAssistant): Unit = {
    var ifs = List.empty[Command]
    var i = 0
    while (i < a.argCount - 1) {
      val pos = a.next
      ifs = ifs :+ new _ifelse
      ifs.last.args = Array(a.arg(i))
      if (i == 0) {
        ifs.last.copyMetadataFrom(this)
      }
      a.add(ifs.last)
      ifs.last.next = a.next
      a.block(i + 1)
      a.goTo()

      // offsets are still relative at this point
      ifs.last.offset = a.next - pos
      i += 2
    }
    // add else block if its there
    if (i == a.argCount - 1) a.block(a.argCount - 1)

    // all branches jump to here, the end of the statement
    a.comeFrom()
  }
}
