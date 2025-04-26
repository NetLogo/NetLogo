// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSetBuilder, Turtle }
import org.nlogo.core.AgentKind
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, SelfScoping }

class _hatch(val breedName: String) extends Command with CustomAssembled with SelfScoping {

  def this() = this("")

  switches = true

  override def toString =
    super.toString + ":" + breedName + ",+" + offset

  override def perform(context: Context): Unit = {
    val count = argEvalIntValue(context, 0)
    if (count > 0) {
      val parent = context.agent.asInstanceOf[Turtle]
      val builder = new AgentSetBuilder(AgentKind.Turtle, count)
      val breed =
        if (breedName.isEmpty) parent.getBreed
        else world.getBreed(breedName)
      var i = 0
      while(i < count) {
        val child = parent.hatch(breed)
        builder.add(child)
        workspace.joinForeverButtons(child)
        i += 1
      }
      context.runExclusiveJob(builder.build(), next)
    }
    context.ip = offset
  }

  def assemble(a: AssemblerAssistant): Unit = {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }

}
