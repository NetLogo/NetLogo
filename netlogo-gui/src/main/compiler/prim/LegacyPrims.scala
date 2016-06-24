// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler.prim

import org.nlogo.compiler.ReplacedPrim
import org.nlogo.core.{ Command, Reporter, Syntax },
  Syntax.{ AgentType, AgentsetType, BooleanType, CodeBlockType, ListType, NumberType,
  NumberBlockType, PatchType, PatchsetType, StringType, SymbolType,
  TurtleType, ReporterBlockType, ReporterTaskType, WildcardType }

case class _patchcol() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = PatchsetType, right = List(NumberType))
}

case class _patchrow() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = PatchsetType, right = List(NumberType))
}


// NOTE: These prims are NOT specific to the GUI as such, but rather are
// primitives which are not in NLW at this time. For use with the NetLogoGUI dialect

package etc {
  case class _approximatehsbold() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType, right = List(NumberType, NumberType, NumberType))
  }

  case class _behaviorspaceexperimentname() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = StringType)
  }

  case class _behaviorspacerunnumber() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType)
  }

  case class _block() extends Reporter {
    def syntax = Syntax.reporterSyntax(right = List(CodeBlockType), ret = StringType)
  }

  case class _butfirst() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = ListType | StringType, right = List(ListType | StringType))
  }

  case class _changelanguage() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _clearallandresetticks() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _distancenowrap() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(PatchType | TurtleType))
  }

  case class _distancexynowrap() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(NumberType, NumberType))
  }

  case class _edit() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _english() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _extracthsbold() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = ListType, right = List(NumberType | ListType))
  }

  case class _face() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "OT--", right = List(PatchType | TurtleType))
  }

  case class _facenowrap() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "OT--", right = List(PatchType | TurtleType))
  }

  case class _facexy() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "OT--", right = List(NumberType, NumberType))
  }

  case class _facexynowrap() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "-T--", right = List(NumberType, NumberType))
  }

  case class _fire() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _git() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(StringType))
  }

  case class _hsbold() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = ListType, right = List(NumberType, NumberType, NumberType))
  }

  case class _hubnetinqsize() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType)
  }

  case class _hubnetmessage() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = WildcardType)
  }

  case class _inradiusnowrap() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = AgentsetType, left = AgentsetType, agentClassString = "-TP-", precedence = 12, right = List(NumberType))
  }

  case class _inconenowrap() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = AgentsetType, left = AgentsetType, agentClassString = "-T--", precedence = 12, right = List(NumberType, NumberType))
  }

  case class _life() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _monitorprecision() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "O---", right = List(WildcardType, NumberType))
  }

  case class _moveto() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "OT--", right = List(PatchType | TurtleType))
  }

  case class _seterrorlocale() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(StringType, StringType))
  }

  case class _spanish() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _towardsnowrap() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(PatchType | TurtleType))
  }

  case class _towardsxynowrap() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(NumberType, NumberType))
  }
}

package gui {
  case class _beep() extends Command {
    def syntax = Syntax.commandSyntax()
  }

  case class _deletelogfiles() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _inspectwithradius() extends Command {
    def syntax = Syntax.commandSyntax(right = List(AgentType, NumberType))
  }

  case class _reload() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _updatemonitor() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(WildcardType))
  }

  case class _ziplogfiles() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(StringType))
  }
}

package dead {
  case class _histogramfrom() extends Command {
    def syntax = Syntax.commandSyntax(right = List(AgentsetType, NumberBlockType))
  }

  case class _hubnetsetclientinterface() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(StringType, ListType))
  }

  case class _randomorrandomfloat() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType, right = List(NumberType))
  }

  case class _moviecancel() extends Command with ReplacedPrim {
    def syntax = Syntax.commandSyntax()
    def recommendedReplacement = "vid:reset-recording"
  }

  case class _movieclose() extends Command with ReplacedPrim {
    def syntax = Syntax.commandSyntax()
    def recommendedReplacement = "vid:save-recording"
  }

  case class _moviegrabinterface() extends Command with ReplacedPrim {
    def syntax = Syntax.commandSyntax()
    def recommendedReplacement = "vid:record-interface"
  }

  case class _moviegrabview() extends Command with ReplacedPrim {
    def syntax = Syntax.commandSyntax()
    def recommendedReplacement = "vid:record-view"
  }

  case class _moviesetframerate() extends Command {
    def syntax = Syntax.commandSyntax(right = List(NumberType))
  }

  case class _moviestart() extends Command with ReplacedPrim {
    def syntax = Syntax.commandSyntax(right = List(StringType))
    def recommendedReplacement = "vid:start-recorder"
  }

  case class _moviestatus() extends Reporter with ReplacedPrim {
    def syntax = Syntax.reporterSyntax(ret = StringType)
    def recommendedReplacement = "vid:recorder-status"
  }
}

package hubnet {
  case class _hubnetbroadcast() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType, WildcardType))
  }

  case class _hubnetbroadcastclearoutput() extends Command {
    def syntax = Syntax.commandSyntax()
  }

  case class _hubnetbroadcastmessage() extends Command {
    def syntax = Syntax.commandSyntax(right = List(WildcardType))
  }

  case class _hubnetbroadcastusermessage() extends Command {
    def syntax = Syntax.commandSyntax(right = List(WildcardType))
  }

  case class _hubnetclearoverride() extends Command {
    def syntax = Syntax.commandSyntax(blockAgentClassString = Some("?"), right = List(StringType, AgentType | AgentsetType, StringType))
  }

  case class _hubnetclearoverrides() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType))
  }

  case class _hubnetclearplot() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType))
  }

  case class _hubnetclientslist() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = ListType)
  }

  case class _hubnetcreateclient() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _hubnetentermessage() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = BooleanType)
  }

  case class _hubnetexitmessage() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = BooleanType)
  }

  case class _hubnetfetchmessage() extends Command {
    def syntax = Syntax.commandSyntax()
  }

  case class _hubnetinqsize() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType)
  }

  case class _hubnetkickallclients() extends Command {
    def syntax = Syntax.commandSyntax()
  }

  case class _hubnetkickclient() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType))
  }

  case class _hubnetmakeplotnarrowcast() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType))
  }

  case class _hubnetmessage() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = WildcardType)
  }

  case class _hubnetmessagesource() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = StringType)
  }

  case class _hubnetmessagetag() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = StringType)
  }

  case class _hubnetmessagewaiting() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = BooleanType)
  }

  case class _hubnetoutqsize() extends Reporter {
    def syntax = Syntax.reporterSyntax(ret = NumberType)
  }

  case class _hubnetplot() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType, NumberType))
  }

  case class _hubnetplotpendown() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType))
  }

  case class _hubnetplotpenup() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType))
  }

  case class _hubnetplotxy() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType, NumberType, NumberType))
  }

  case class _hubnetreset() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---")
  }

  case class _hubnetresetperspective() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType))
  }

  case class _hubnetroboclient() extends Command {
    def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(NumberType))
  }

  case class _hubnetsend() extends Command {
    def syntax = Syntax.commandSyntax(right = List(ListType | StringType, StringType, WildcardType))
  }

  case class _hubnetsendclearoutput() extends Command {
    def syntax = Syntax.commandSyntax(right = List(ListType | StringType))
  }

  case class _hubnetsendfollow() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType, AgentType, NumberType))
  }

  case class _hubnetsendfromlocalclient() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType, StringType, WildcardType))
  }

  case class _hubnetsendmessage() extends Command {
    def syntax = Syntax.commandSyntax(right = List(ListType | StringType, WildcardType))
  }

  case class _hubnetsendoverride() extends Command {
    def syntax = Syntax.commandSyntax(blockAgentClassString = Some("?"), right = List(StringType, AgentType | AgentsetType, StringType, ReporterBlockType))
  }

  case class _hubnetsendusermessage() extends Command {
    def syntax = Syntax.commandSyntax(right = List(ListType | StringType, WildcardType))
  }

  case class _hubnetsendwatch() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType, AgentType))
  }

  case class _hubnetsethistogramnumbars() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType, NumberType))
  }

  case class _hubnetsetplotmirroring() extends Command {
    def syntax = Syntax.commandSyntax(right = List(BooleanType))
  }

  case class _hubnetsetplotpeninterval() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType, NumberType))
  }

  case class _hubnetsetplotpenmode() extends Command {
    def syntax = Syntax.commandSyntax(right = List(StringType, NumberType))
  }

  case class _hubnetsetviewmirroring() extends Command {
    def syntax = Syntax.commandSyntax(right = List(BooleanType))
  }

  case class _hubnetwaitforclients() extends Command {
    def syntax = Syntax.commandSyntax(right = List(NumberType, NumberType))
  }

  case class _hubnetwaitformessages() extends Command {
    def syntax = Syntax.commandSyntax(right = List(NumberType, NumberType))
  }
}
