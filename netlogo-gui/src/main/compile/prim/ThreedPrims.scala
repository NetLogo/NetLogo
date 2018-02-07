// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.prim.threed

import org.nlogo.core.{ Reporter, Command, Syntax },
  Syntax.{ NumberType, TurtlesetType, PatchType, NobodyType, PatchsetType, StringType, TurtleType }

case class _breedat(breedName: String) extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = TurtlesetType, agentClassString = "-TP-", right = List(NumberType, NumberType, NumberType))
}

case class _distancexyz() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(NumberType, NumberType, NumberType))
}

case class _distancexyznowrap() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(NumberType, NumberType, NumberType))
}

case class _dz() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-T--")
}

case class _face() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "OT--", right = List(PatchType | TurtleType))
}

case class _facexyz() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "OT--", right = List(NumberType, NumberType, NumberType))
}

case class _linkpitch() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "---L")
}

case class _load3Dshapes() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(StringType))
}

case class _maxpzcor() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType)
}

case class _minpzcor() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType)
}

case class _neighbors6() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = PatchsetType, agentClassString = "-TP-")
}

case class _netlogoversion() extends Reporter {
  override def syntax = Syntax.reporterSyntax(ret = Syntax.StringType)
}

case class _oheading() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType)
}

case class _opitch() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType)
}

case class _orbitdown() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(NumberType))
}

case class _orbitleft() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(NumberType))
}

case class _orbitright() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(NumberType))
}

case class _orbitup() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(NumberType))
}

case class _oroll() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType)
}

case class _oxcor() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "O---")
}

case class _oycor() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "O---")
}

case class _ozcor() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "O---")
}

case class _patch() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = PatchType | NobodyType, right = List(NumberType, NumberType, NumberType))
}

case class _patchat() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = PatchType, agentClassString = "-TP-", right = List(NumberType, NumberType, NumberType))
}

case class _patchatheadingpitchanddistance() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = PatchType, agentClassString = "-TP-", right = List(NumberType, NumberType, NumberType))
}

case class _randompzcor() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType)
}

case class _randomzcor() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType)
}

case class _resizeworld() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(NumberType, NumberType, NumberType, NumberType, NumberType, NumberType))
}

case class _rollleft() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "-T--", right = List(NumberType))
}

case class _rollright() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "-T--", right = List(NumberType))
}

case class _setxyz() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "OT--", right = List(NumberType, NumberType, NumberType))
}

case class _tiltdown() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "-T--", right = List(NumberType))
}

case class _tiltup() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "-T--", right = List(NumberType))
}

case class _towardspitch() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(PatchType | TurtleType))
}

case class _towardspitchnowrap() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(PatchType | TurtleType))
}

case class _towardspitchxyz() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(NumberType, NumberType, NumberType))
}

case class _towardspitchxyznowrap() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType, agentClassString = "-TP-", right = List(NumberType, NumberType, NumberType))
}

case class _turtlesat() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = TurtlesetType, agentClassString = "-TP-", right = List(NumberType, NumberType, NumberType))
}

case class _worlddepth() extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = NumberType)
}

case class _zoom() extends Command {
  def syntax = Syntax.commandSyntax(agentClassString = "O---", right = List(NumberType))
}
