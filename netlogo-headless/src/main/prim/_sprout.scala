// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSetBuilder, Patch }
import org.nlogo.core.AgentKind
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, SelfScoping }

class _sprout(val breedName: String)
  extends Command
  with CustomAssembled
  with SelfScoping {

  def this() = this("")

  switches = true

  override def toString =
    super.toString + ":" + breedName + ",+" + offset

  override def perform(context: Context): Unit = {
    val parent = context.agent.asInstanceOf[Patch]
    val count = argEvalIntValue(context, 0)
    val random = context.job.random
    if (count > 0) {
      val builder = new AgentSetBuilder(AgentKind.Turtle, count)
      val breed =
        if (breedName.isEmpty) world.turtles
        else world.getBreed(breedName)
      var i = 0
      while (i < count) {
        val child = parent.sprout(random.nextInt(14), random.nextInt(360), breed)
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
