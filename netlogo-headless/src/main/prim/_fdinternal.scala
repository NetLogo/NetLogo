// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.Turtle
import org.nlogo.api.{ AgentException, Numbers }
import org.nlogo.core.Let
import org.nlogo.nvm.{ Command, Context, MutableDouble }

class _fdinternal(let: Let) extends Command {

  def this(original: _fd) = this(original.let)
  def this(original: _bk) = this(original.let)

  switches = true

  override def perform(context: Context): Unit = {
    perform_1(context)
  }

  def perform_1(context: Context): Unit = {
    val turtle = context.agent.asInstanceOf[Turtle]
    val countdown = context.activation.binding.getLet(let).asInstanceOf[MutableDouble]
    val distance = countdown.value
    val distanceMagnitude = StrictMath.abs(distance)
    if (distanceMagnitude <= Numbers.Infinitesimal)
      context.ip = next
    else if (distanceMagnitude <= 1.0) {
      try turtle.jump(distance)
      catch { case _: AgentException => }
      context.ip = next
    } else {
      val stepDistance = if (distance > 0) 1 else -1
      try {
        turtle.jump(stepDistance)
        countdown.value -= stepDistance
      }
      catch { case _: AgentException =>
        context.ip = next
      }
    }
  }
}
