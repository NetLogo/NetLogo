// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim.hubnet

case class _hubnetbroadcast() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.WildcardType))
}

case class _hubnetbroadcastclearoutput() extends Command {
  def syntax = Syntax.commandSyntax()
}

case class _hubnetbroadcastmessage() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.WildcardType))
}

case class _hubnetclearoverride() extends Command {
  def syntax = Syntax.commandSyntax(
    blockAgentClassString = Some("?"),
    right = List(
      Syntax.StringType,
      Syntax.AgentType | Syntax.AgentsetType,
      Syntax.StringType))
}

case class _hubnetclearoverrides() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.StringType))
}

case class _hubnetclearplot() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.StringType))
}

case class _hubnetcreateclient() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "O---")
}

case class _hubnetclientslist() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = Syntax.ListType)
}

case class _hubnetentermessage() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = Syntax.BooleanType)
}

case class _hubnetexitmessage() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = Syntax.BooleanType)
}

case class _hubnetfetchmessage() extends Command {
  def syntax = Syntax.commandSyntax()
}

case class _hubnetkickallclients() extends Command {
  def syntax = Syntax.commandSyntax()
}

case class _hubnetkickclient() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.StringType))
}

case class _hubnetmakeplotnarrowcast() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.StringType))
}

case class _hubnetmessage() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = Syntax.WildcardType)
}

case class _hubnetmessagesource() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = Syntax.StringType)
}

case class _hubnetmessagetag() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = Syntax.StringType)
}

case class _hubnetmessagewaiting() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = Syntax.BooleanType)
}

case class _hubnetplot() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.NumberType))
}

case class _hubnetreset() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "O---")
}

case class _hubnetresetperspective() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.StringType))
}

case class _hubnetsend() extends Command {
  def syntax = Syntax.commandSyntax(
    right = List(
      Syntax.ListType | Syntax.StringType,
      Syntax.StringType,
      Syntax.WildcardType))
}

case class _hubnetsendclearoutput() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.ListType | Syntax.StringType))
}

case class _hubnetsendfollow() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.AgentType, Syntax.NumberType))
}

case class _hubnetsendmessage() extends Command {
  def syntax = Syntax.commandSyntax(
    right = List(Syntax.ListType | Syntax.StringType, Syntax.WildcardType))
}

case class _hubnetsendoverride() extends Command {
  def syntax = Syntax.commandSyntax(
    blockAgentClassString = Some("?"),
    right = List(
      Syntax.StringType,
      Syntax.AgentType | Syntax.AgentsetType,
      Syntax.StringType,
      Syntax.ReporterBlockType))
}

case class _hubnetsendwatch() extends Command {
  def syntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.AgentType))
}

