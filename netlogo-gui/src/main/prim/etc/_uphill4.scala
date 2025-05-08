// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import java.util.{ ArrayList => JArrayList }
import org.nlogo.agent.{ Patch, Turtle }
import org.nlogo.api.AgentException
import org.nlogo.core.Reference
import org.nlogo.nvm.{ Command, Context, Referencer }

class _uphill4 extends Command with Referencer {
  this.switches = true;

  override def referenceIndex: Int = 0

  override def applyReference(ref: Reference): Command = {
    reference = ref
    this
  }

  private var reference: Reference = null

  override def toString: String = {
    if (world != null && reference != null)
      super.toString + ":" + world.patchesOwnNameAt(reference.vn)
    else
      super.toString + ":" + reference.vn
  }

  override def perform(context: Context): Unit = {
    perform_1(context)
  }

  def perform_1(context: Context): Unit = {
    val turtle = context.agent.asInstanceOf[Turtle]
    turtle.moveToPatchCenter()
    val patch = turtle.getPatchHere()
    var winningValue = - Double.MaxValue
    val winners = new JArrayList[Patch]()
    val iter = patch.getNeighbors4().iterator
    while (iter.hasNext) {
      val tester = iter.next().asInstanceOf[Patch]
      val value = tester.getPatchVariable(reference.vn)
      if (value.isInstanceOf[java.lang.Double]) {
        val dvalue = value.asInstanceOf[java.lang.Double].doubleValue();
        // need to be careful here to handle properly the case where
        // dvalue equals - Double.MAX_VALUE - ST 10/11/04, 1/6/07
        if (dvalue >= winningValue) {
          if (dvalue > winningValue) {
            winningValue = dvalue
            winners.clear()
          }
          winners.add(tester)
        }
      }
    }
    if (! winners.isEmpty && (!(patch.getPatchVariable(reference.vn).isInstanceOf[java.lang.Double]) ||
            winningValue > patch.getPatchVariable(reference.vn).asInstanceOf[java.lang.Double].doubleValue)) {
      val winner = winners.get(context.job.random.nextInt(winners.size))
      turtle.face(winner, true)
      try {
        turtle.moveTo(winner)
      } catch {
        // should be impossible
        case ex: AgentException => throw new IllegalStateException(ex)
      }
    }
    context.ip = next
  }
}
