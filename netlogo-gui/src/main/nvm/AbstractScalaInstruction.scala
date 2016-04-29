// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ AgentKind, Token, TokenHolder }

object AbstractScalaInstruction {
  def agentKindDescription(kind: AgentKind): String =
    kind match {
      case AgentKind.Observer =>
        "the observer"
      case AgentKind.Turtle =>
        "a turtle"
      case AgentKind.Patch =>
        "a patch"
      case AgentKind.Link =>
        "a link"
      case _ =>
        null
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
        (kind, c) <- pairs
        if agentClassString.contains(c)
      }
      yield kind
    throw new EngineException(context, this,
      "this code can't be run by " + AbstractScalaInstruction.agentKindDescription(kind) +
      (if (allowedKinds.size == 1)
         ", only " + AbstractScalaInstruction.agentKindDescription(allowedKinds.head)
       else
         ""))
  }
}
