// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentException }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _setlinkbreedvariable(name: String) extends Command {

  def this(original: _linkbreedvariable) = this(original.name)

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.WildcardType), "---L", true)

  override def toString =
    super.toString + ":" + name

  override def perform(context: Context) {
    val value = args(0).report(context)
    try context.agent.setLinkBreedVariable(name, value)
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }
    context.ip = next
  }

}
