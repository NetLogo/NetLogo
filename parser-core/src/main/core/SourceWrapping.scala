// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object SourceWrapping {

  val agentKindHint = Map[AgentKind, String](
    AgentKind.Observer -> "__observercode",
    AgentKind.Turtle -> "__turtlecode",
    AgentKind.Patch -> "__patchcode",
    AgentKind.Link -> "__linkcode")

  def getHeader(kind: AgentKind, commands: Boolean) = {
    val hint = agentKindHint(kind)
    if(commands) "to __evaluator [] " + hint + " "
    else
      // we put parens around what comes after "report", because we want to make
      // sure we don't let a malformed reporter like "3 die" past the compiler, since
      // "to-report foo report 3 die end" is syntactically valid but
      // "to-report foo report (3 die) end" isn't. - ST 11/12/09
      "to-report __evaluator [] " + hint + " report ( "
  }

  def getFooter(commands: Boolean) =
    if(commands) "\n__done end" else "\n) __done end"

  def sourceOffset(kind: AgentKind, commands: Boolean): Int =
    getHeader(kind, commands).length

}
