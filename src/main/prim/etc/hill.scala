// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm,
  org.nlogo.core.Syntax,
  org.nlogo.agent.{ Turtle, Patch, AgentSet }

class _uphill extends HillCommand(sign = 1) {
  override def neighbors(p: Patch) = p.getNeighbors }
class _uphill4 extends HillCommand(sign = 1) {
  override def neighbors(p: Patch) = p.getNeighbors4 }
class _downhill extends HillCommand(sign = -1) {
  override def neighbors(p: Patch) = p.getNeighbors }
class _downhill4 extends HillCommand(sign = -1) {
  override def neighbors(p: Patch) = p.getNeighbors4 }

abstract class HillCommand(sign: Int) extends nvm.Command {

  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.ReferenceType),
      agentClassString = "-T--",
      switches = true)

  override def toString =
    super.toString +
      (if (reference != null && world != null)
         ":" + world.patchesOwnNameAt(reference.vn)
       else
         "")

  def neighbors(patch: Patch): AgentSet // abstract

  override def perform(context: nvm.Context) {
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
