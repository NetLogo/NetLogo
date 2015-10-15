// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim.etc

import org.nlogo.core.Pure

//scalastyle:off number.of.types
case class _all() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType, Syntax.BooleanBlockType),
      ret = Syntax.BooleanType,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _approximatehsb() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _approximatergb() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _atpoints() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.TurtlesetType | Syntax.PatchsetType,
      right = List(Syntax.ListType),
      ret = Syntax.AgentsetType,
      precedence = Syntax.NormalPrecedence + 2)
}
case class _butfirst() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType | Syntax.StringType),
      ret = Syntax.ListType | Syntax.StringType)
}
case class _butlast() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType | Syntax.StringType),
      ret = Syntax.ListType | Syntax.StringType)
}
case class _canmove() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.BooleanType,
      agentClassString = "-T--")
}
case class _checksyntax() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.StringType),
      ret = Syntax.StringType)
}
case class _die() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T-L")
}
case class _distance() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.TurtleType | Syntax.PatchType),
      ret = Syntax.NumberType,
      agentClassString = "-TP-")
}
case class _div() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType,
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType,
      precedence = Syntax.NormalPrecedence - 2)
}
case class _empty() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType | Syntax.StringType),
      ret = Syntax.BooleanType)
}
case class _exportdrawing() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
}
case class _exportoutput() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
}
case class _exportview() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
}
case class _exportworld() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
}
case class _extracthsb() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType | Syntax.ListType),
      ret = Syntax.ListType)
}
case class _extractrgb() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.ListType)
}
case class _first() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType | Syntax.StringType),
      ret = Syntax.WildcardType)
}
case class _follow() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.TurtleType),
      agentClassString = "O---")
}
case class _git() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType),
      agentClassString = "O---")
}
case class _greaterorequal() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType | Syntax.StringType | Syntax.AgentType,
      right = List(Syntax.NumberType | Syntax.StringType | Syntax.AgentType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 4)
}
case class _hsb() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.ListType)
}
case class _importdrawing() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType),
      agentClassString = "O---")
}
case class _importpcolorsrgb() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType),
      agentClassString = "O---")
}
case class _importworld() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType),
      agentClassString = "O---")
}
case class _incone() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.TurtlesetType | Syntax.PatchsetType,
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.TurtlesetType | Syntax.PatchsetType,
      precedence = Syntax.NormalPrecedence + 2,
      agentClassString = "-T--")
}
case class _item() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.ListType | Syntax.StringType),
      ret = Syntax.WildcardType)
}
case class _last() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType | Syntax.StringType),
      ret = Syntax.WildcardType)
}
case class _layoutcircle() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.TurtlesetType | Syntax.ListType, Syntax.NumberType))
}
case class _layoutradial() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.TurtlesetType, Syntax.LinksetType, Syntax.TurtleType))
}
case class _layoutspring() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.TurtlesetType, Syntax.LinksetType, Syntax.NumberType,
        Syntax.NumberType, Syntax.NumberType))
}
case class _layouttutte() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.TurtlesetType, Syntax.LinksetType, Syntax.NumberType))
}
case class _length() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType | Syntax.StringType),
      ret = Syntax.NumberType)
}
case class _lessorequal() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType | Syntax.StringType | Syntax.AgentType,
      right = List(Syntax.NumberType | Syntax.StringType | Syntax.AgentType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 4)
}
case class _linkheading() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType,
      agentClassString = "---L")
}
case class _linkset() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.LinkType | Syntax.LinksetType |
        Syntax.NobodyType | Syntax.ListType),
      ret = Syntax.LinksetType,
      defaultOption = Some(1),
      minimumOption = Some(0))
}
case class _ln() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _log() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _makepreview() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _max() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.NumberType)
}
case class _maxnof() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.AgentsetType, Syntax.NumberBlockType),
      ret = Syntax.AgentsetType,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _maxoneof() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType, Syntax.NumberBlockType),
      ret = Syntax.AgentType,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _mean() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.NumberType)
}
case class _median() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.NumberType)
}
case class _member() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType, Syntax.ListType | Syntax.StringType | Syntax.AgentsetType),
      ret = Syntax.BooleanType)
}
case class _min() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.NumberType)
}
case class _minnof() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.AgentsetType, Syntax.NumberBlockType),
      ret = Syntax.AgentsetType,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _minoneof() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType, Syntax.NumberBlockType),
      ret = Syntax.AgentType,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _mod() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType,
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType,
      precedence = Syntax.NormalPrecedence - 2)
}
case class _modes() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.ListType)
}
case class _moveto() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.TurtleType | Syntax.PatchType),
      agentClassString = "-T--")
}
case class _myself() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentType,
      agentClassString = "-TPL")
}
case class _nof() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.AgentsetType | Syntax.ListType),
      ret = Syntax.AgentsetType | Syntax.ListType)
}
case class _otherend() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentType,
      agentClassString = "-T-L")
}
case class _patchahead() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.PatchType,
      agentClassString = "-T--")
}
case class _patchatheadinganddistance() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.PatchType,
      agentClassString = "-TP-")
}
case class _patchset() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.PatchType | Syntax.PatchsetType | Syntax.NobodyType | Syntax.ListType),
      ret = Syntax.PatchsetType,
      defaultOption = Some(1),
      minimumOption = Some(0))
}
case class _position() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType, Syntax.ListType | Syntax.StringType),
      ret = Syntax.NumberType | Syntax.BooleanType)
}
case class _randomgamma() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _remainder() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _remove() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType, Syntax.ListType | Syntax.StringType),
      ret = Syntax.ListType | Syntax.StringType)
}
case class _removeduplicates() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.ListType)
}
case class _removeitem() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.ListType | Syntax.StringType),
      ret = Syntax.ListType | Syntax.StringType)
}
case class _replaceitem() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.ListType | Syntax.StringType, Syntax.WildcardType),
      ret = Syntax.ListType | Syntax.StringType)
}
case class _report() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _resizeworld() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType, Syntax.NumberType, Syntax.NumberType),
      agentClassString = "O---")
}
case class _reverse() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType | Syntax.StringType),
      ret = Syntax.ListType | Syntax.StringType)
}
case class _rgb() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.ListType)
}
case class _ride() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.TurtleType),
      agentClassString = "O---")
}
case class _scalecolor() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType, Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _setpatchsize() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "O---")
}
case class _setxy() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      agentClassString = "-T--")
}
case class _shadeof() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.BooleanType)
}
case class _shuffle() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.ListType)
}
case class _sort() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType | Syntax.AgentsetType),
      ret = Syntax.ListType)
}
case class _sqrt() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _standarddeviation() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.NumberType)
}
case class _stop() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _sublist() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType, Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.ListType)
}
case class _substring() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.StringType, Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.StringType)
}
case class _ticks() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _towards() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.TurtleType | Syntax.PatchType),
      ret = Syntax.NumberType,
      agentClassString = "-TP-")
}
case class _towardsxy() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType,
      agentClassString = "-TP-")
}
case class _turtlesat() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.TurtlesetType,
      agentClassString = "-TP-")
}
case class _turtleset() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.TurtleType | Syntax.TurtlesetType | Syntax.NobodyType | Syntax.ListType),
      ret = Syntax.TurtlesetType,
      defaultOption = Some(1),
      minimumOption = Some(0))
}
case class _turtleshere() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.TurtlesetType,
      agentClassString = "-TP-")
}
case class _turtleson() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentType | Syntax.AgentsetType),
      ret = Syntax.TurtlesetType)
}
case class _variance() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.NumberType)
}
case class _watch() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.AgentType),
      agentClassString = "O---")
}
case class _while() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.BooleanBlockType, Syntax.CommandBlockType))
}
case class _withlocalrandomness() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.CommandBlockType),
      introducesContext = true)
}
case class _withmax() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.AgentsetType,
      right = List(Syntax.NumberBlockType),
      ret = Syntax.AgentsetType,
      precedence = Syntax.NormalPrecedence + 2,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _withmin() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.AgentsetType,
      right = List(Syntax.NumberBlockType),
      ret = Syntax.AgentsetType,
      precedence = Syntax.NormalPrecedence + 2,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _withoutinterruption() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.CommandBlockType),
      introducesContext = true)
}
case class _xor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.BooleanType,
      right = List(Syntax.BooleanType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 6)
}
//scalastyle:on number.of.types
