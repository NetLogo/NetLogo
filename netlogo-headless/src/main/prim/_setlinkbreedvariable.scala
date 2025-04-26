// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Command, Context}
import org.nlogo.nvm.RuntimePrimitiveException

class _setlinkbreedvariable(name: String) extends Command {

  def this(original: _linkbreedvariable) = this(original.name)

  switches = true

  override def toString =
    super.toString + ":" + name

  override def perform(context: Context): Unit = {
    val value = args(0).report(context)
    try context.agent.setLinkBreedVariable(name, value)
    catch { case ex: AgentException =>
      throw new RuntimePrimitiveException(context, this, ex.getMessage) }
    context.ip = next
  }

}
