// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Reference
import org.nlogo.agent.{ AgentSet, Patch, Turtle }
import org.nlogo.nvm

class _uphill extends HillCommand(sign = 1) {
  override def neighbors(p: Patch) = p.getNeighbors }
class _uphill4 extends HillCommand(sign = 1) {
  override def neighbors(p: Patch) = p.getNeighbors4 }
class _downhill extends HillCommand(sign = -1) {
  override def neighbors(p: Patch) = p.getNeighbors }
class _downhill4 extends HillCommand(sign = -1) {
  override def neighbors(p: Patch) = p.getNeighbors4 }

abstract class HillCommand(sign: Int) extends nvm.Command with nvm.Referencer {
  switches = true

  override def referenceIndex: Int = 0

  private var reference: Reference = null

  override def applyReference(ref: Reference): nvm.Command = {
    reference = ref
    this
  }

  override def toString =
    super.toString +
      (if (reference != null && world != null)
         ":" + world.patchesOwnNameAt(reference.vn)
       else
         "")

  def neighbors(patch: Patch): AgentSet // abstract

  override def perform(context: nvm.Context): Unit = {
    val turtle = context.agent.asInstanceOf[Turtle]
    turtle.moveToPatchCenter()
    val patch = turtle.getPatchHere()
    var winningValue = Double.MinValue
    val winners = collection.mutable.Buffer[Patch]()
    val it = neighbors(patch).iterator
    while (it.hasNext) {
      val tester = it.next().asInstanceOf[Patch]
      tester.getPatchVariable(reference.vn) match {
        case value: java.lang.Double =>
          val dvalue = value.doubleValue * sign
          // need to be careful here to handle properly the case where
          // dvalue equals -Double.MinValue - ST 10/11/04, 1/6/07, 4/5/14
          if (dvalue >= winningValue) {
            if (dvalue > winningValue) {
              winningValue = dvalue;
              winners.clear()
            }
            winners += tester
          }
        case _ => // skip
      }
    }
    if (!winners.isEmpty)
      patch.getPatchVariable(reference.vn) match {
        case valueHere: java.lang.Double =>
          if (winningValue > valueHere.doubleValue * sign) {
            val target = winners(context.job.random.nextInt(winners.size))
            turtle.face(target, true)
            turtle.moveTo(target)
          }
        case _ =>
      }
    context.ip = next
  }

}
