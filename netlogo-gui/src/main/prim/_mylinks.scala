// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Turtle }
import org.nlogo.core.Syntax
import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Context, Reporter }

class _mylinks(private[this] val _breedName: String) extends Reporter {
  def this() = this(null)

  override def syntax: Syntax =
    Syntax.reporterSyntax(ret = Syntax.AgentsetType, agentClassString = "-T--")

  @Override
  override def toString: String = s"${super.toString}:${_breedName}"

  @Override
  def report(context: Context): AnyRef = {
    val breed =
      if (_breedName != null) world.getLinkBreed(_breedName)
      else world.links()
    world.linkManager.findLinksWith(context.agent.asInstanceOf[Turtle], breed)
  }
}
