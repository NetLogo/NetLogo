// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentException }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _setbreedvariable(name: String) extends Command {

  def this(original: _breedvariable) = this(original.name)

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.WildcardType), "-T--", true)

  override def toString =
    super.toString + ":" + name

  override def perform(context: Context) {
    val value = args(0).report(context)
    try context.agent.setBreedVariable(name, value)
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }
    context.ip = next
  }

}
