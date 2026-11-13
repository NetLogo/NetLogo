// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ Agent, Link, Turtle }
import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ Command, Context}
import org.nlogo.nvm.RuntimePrimitiveException

class _face extends Command {
  switches = true
  override def perform(context: Context): Unit = {
    perform_1(context, argEvalAgent(context, 0))
  }
  def perform_1(context: Context, target: Agent): Unit = {
    if(target.isInstanceOf[Link])
      throw new RuntimePrimitiveException(context, this, loc"Expected a turtle or a patch but got a link.")
    if (target.id == -1)
      throw new RuntimePrimitiveException(context, this, loc"That ${target.classDisplayName} is dead.")
    context.agent.asInstanceOf[Turtle].face(target, true)
    context.ip = next
  }
}
