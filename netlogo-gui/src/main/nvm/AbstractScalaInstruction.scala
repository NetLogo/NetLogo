// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ AgentKind, I18N, Token, TokenHolder }

object AbstractScalaInstruction {
  def agentKindDescription(kind: AgentKind): String = {
    val key =
      kind match {
        case AgentKind.Observer => "observer"
        case AgentKind.Turtle   => "turtle"
        case AgentKind.Patch    => "patch"
        case AgentKind.Link     => "link"
        case _                  => null
      }
    if (key == null) null
    else I18N.errors.get("org.nlogo.prim.$common.agentKind." + key)
  }


  def agentKindError(kind: AgentKind, allowedKinds: Seq[AgentKind]): String = {
    val kindDescription = agentKindDescription(kind)
    lazy val allowedDescription = agentKindDescription(allowedKinds.head)
    if (allowedKinds.size == 1)
      I18N.errors.getN("org.nlogo.prim.$common.invalidAgentKind.alternative", kindDescription, allowedDescription)
    else
      I18N.errors.getN("org.nlogo.prim.$common.invalidAgentKind.simple", kindDescription)
  }
}

abstract class AbstractScalaInstruction extends TokenHolder { this: Instruction =>
  var token: Token = null

  def throwAgentClassException(context: Context, kind: AgentKind): Nothing = {
    val pairs =
      Seq(
        (AgentKind.Observer, 'O'), (AgentKind.Turtle, 'T'),
        (AgentKind.Patch, 'P'), (AgentKind.Link, 'L'))
    val allowedKinds =
      for {
        (thisKind, c) <- pairs
        if agentClassString.contains(c)
      } yield thisKind

    throw new EngineException(context, this, AbstractScalaInstruction.agentKindError(kind, allowedKinds))
  }
}
