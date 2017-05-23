// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim.etc

import org.nlogo.core.Pure

//scalastyle:off file.size.limit
//scalastyle:off number.of.types
case class _abs() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _acos() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _apply() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.CommandType, Syntax.ListType))
}
case class _applyresult() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ReporterType, Syntax.ListType),
      ret = Syntax.WildcardType)
}
case class _asin() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _atan() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _basecolors() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.ListType)
}
case class _beep() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _behaviorspacerunnumber() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _bench() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      agentClassString = "O---")
}
case class _block() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.CodeBlockType),
      ret = Syntax.StringType)
}
case class _boom() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType)
}
case class _bothends() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentsetType,
      agentClassString = "---L")
}
case class _ceil() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _changetopology() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.BooleanType, Syntax.BooleanType))
}
case class _checksum() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType,
      agentClassString = "O---")
}
case class _clearall() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _clearallandresetticks() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _cleardrawing() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _clearglobals() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _clearlinks() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _clearoutput() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _clearpatches() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _clearticks() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _clearturtles() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _cos() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _dateandtime() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)
}
case class _diffuse() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.ReferenceType, Syntax.NumberType),
      agentClassString = "O---")
}
case class _diffuse4() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.ReferenceType, Syntax.NumberType),
      agentClassString = "O---")
}
case class _display() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _distancexy() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType,
      agentClassString = "-TP-")
}
case class _downhill() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.ReferenceType),
      agentClassString = "-T--")
}
case class _downhill4() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.ReferenceType),
      agentClassString = "-T--")
}
case class _dump() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType,
      agentClassString = "O---")
}
case class _dump1() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)
}
case class _dumpextensionprims() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)
}
case class _dumpextensions() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)
}
case class _dx() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType,
      agentClassString = "-T--")
}
case class _dy() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType,
      agentClassString = "-T--")
}
case class _error() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _every() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType))
}
case class _exp() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _experimentstepend() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _exportinterface() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
}
case class _face() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.TurtleType | Syntax.PatchType),
      agentClassString = "-T--")
}
case class _facexy() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      agentClassString = "-T--")
}
case class _fileatend() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.BooleanType)
}
case class _fileclose() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _filecloseall() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _filedelete() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
}
case class _fileexists() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.StringType),
      ret = Syntax.BooleanType)
}
case class _fileflush() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _fileopen() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
}
case class _fileprint() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _fileread() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.ReadableType)
}
case class _filereadchars() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.StringType)
}
case class _filereadline() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)
}
case class _fileshow() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _filetype() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _filewrite() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.ReadableType))
}
case class _filter() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ReporterType, Syntax.ListType),
      ret = Syntax.ListType)
}
case class _floor() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _followme() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--")
}
case class _foreach() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(
        Syntax.RepeatableType | Syntax.ListType,
        Syntax.CommandType),
      defaultOption = Some(2))
}
case class _foreverbuttonend() extends Command {
  override def syntax =
    Syntax.commandSyntax(canBeConcise = false)
}
case class _fput() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType, Syntax.ListType),
      ret = Syntax.ListType)
}
case class _hidelink() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "---L")
}
case class _hideturtle() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--")
}
case class _home() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--")
}
case class _if() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.BooleanType, Syntax.CommandBlockType))
}
case class _ifelse() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(
        Syntax.BooleanType,
        Syntax.CommandBlockType,
        Syntax.CommandBlockType))
}
case class _ifelsevalue() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(
        Syntax.BooleanType,
        Syntax.ReporterBlockType,
        Syntax.ReporterBlockType),
      ret = Syntax.WildcardType,
      precedence = Syntax.NormalPrecedence - 7)
}
case class _ignore() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _importpatchcolors() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType),
      agentClassString = "O---")
}
case class _insertitem() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.ListType | Syntax.StringType, Syntax.WildcardType),
      ret = Syntax.ListType | Syntax.StringType)
}
case class _inspect() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.AgentType))
}
case class _int() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _isagent() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isagentset() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isboolean() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isanonymouscommand() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isdirectedlink() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _islink() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _islinkset() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _islist() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isnumber() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _ispatch() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _ispatchset() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isanonymousreporter() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isstring() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isturtle() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isturtleset() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _isundirectedlink() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _left() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "-T--")
}
case class _link() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.LinkType | Syntax.NobodyType)
}
case class _linkcode() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "---L",
      canBeConcise = false)
}
case class _linklength() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType,
      agentClassString = "---L")
}
case class _links() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.LinksetType)
}
case class _linkshapes() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.ListType)
}
case class _loop() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.CommandBlockType))
}
case class _lput() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType, Syntax.ListType),
      ret = Syntax.ListType)
}
case class _map() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ReporterType, Syntax.RepeatableType | Syntax.ListType),
      ret = Syntax.ListType,
      defaultOption = Some(2))
}
case class _maxpxcor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _maxpycor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _minpxcor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _minpycor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _mkdir() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
}
case class _mousedown() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.BooleanType)
}
case class _mouseinside() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.BooleanType)
}
case class _mousexcor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _mouseycor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _mult() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType,
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType,
      precedence = Syntax.NormalPrecedence - 2)
}
case class _nanotime() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _netlogoapplet() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.BooleanType)
}
case class _netlogoversion() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)
}
case class _netlogoweb() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.BooleanType)
}
case class _newseed() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _nodisplay() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _nolinks() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.LinksetType)
}
case class _nopatches() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchsetType)
}
case class _noturtles() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.TurtlesetType)
}
case class _nvalues() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.ReporterType),
      ret = Syntax.ListType)
}
case class _observercode() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---",
      canBeConcise = false)
}
case class _patch() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.PatchType | Syntax.NobodyType)
}
case class _patchcode() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "--P-",
      canBeConcise = false)
}
case class _patchhere() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchType,
      agentClassString = "-T--")
}
case class _patchleftandahead() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.PatchType,
      agentClassString = "-T--")
}
case class _patchrightandahead() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.PatchType,
      agentClassString = "-T--")
}
case class _patchsize() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _pendown() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--")
}
case class _penerase() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--")
}
case class _penup() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--")
}
case class _plus() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType,
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType,
      precedence = Syntax.NormalPrecedence - 3)
}
case class _pow() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType,
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType,
      precedence = Syntax.NormalPrecedence - 1)
}
case class _precision() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _processors() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _pwd() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _randomexponential() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _randomfloat() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _randomnormal() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _randompoisson() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _randompxcor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _randompycor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _randomseed() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType))
}
case class _randomstate() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)
}
case class _randomxcor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _randomycor() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _range() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType | Syntax.RepeatableType),
      defaultOption = Option(1),
      minimumOption = Option(1),
      ret = Syntax.ListType)
}
case class _readfromstring() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.StringType),
      ret = Syntax.ReadableType)
}
case class _reduce() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ReporterType, Syntax.ListType),
      ret = Syntax.WildcardType)
}
case class _reference() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ReferenceType),
      ret = Syntax.ListType)
}
case class _reloadextensions() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "OTPL")
}
case class _resetperspective() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "OTPL")
}
case class _resetticks() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _resettimer() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _rideme() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--")
}
case class _right() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "-T--")
}
case class _round() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _runresult() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(
        Syntax.StringType | Syntax.ReporterType,
        Syntax.RepeatableType | Syntax.WildcardType),
      ret = Syntax.WildcardType,
      defaultOption = Some(1))
}
case class _self() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentType,
      agentClassString = "-TPL")
}
case class _setcurdir() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
}
case class _setdefaultshape() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right =
        List(
          Syntax.TurtlesetType | Syntax.LinksetType,
          Syntax.StringType),
      agentClassString = "O---")
}
case class _setlinethickness() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "-T--")
}
case class _shapes() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.ListType)
}
case class _showlink() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "---L")
}
case class _showturtle() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--")
}
case class _sin() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _sortby() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ReporterType, Syntax.ListType | Syntax.AgentsetType),
      ret = Syntax.ListType,
      blockAgentClassString = Option("?"))
}
case class _sorton() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ReporterBlockType, Syntax.AgentsetType),
      ret = Syntax.ListType,
      blockAgentClassString = Option("?"))
}
case class _stacktrace() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)
}
case class _stamp() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T-L")
}
case class _stamperase() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T-L")
}
case class _stderr() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _stdout() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _stopinspecting() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.AgentType))
}
case class _stopinspectingdeadagents() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _subject() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentType)
}
case class _subtractheadings() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _symbolstring() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.SymbolType),
      ret = Syntax.StringType)
}
case class _tan() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _thunkdidfinish() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _tick() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---")
}
case class _tickadvance() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "O---")
}
case class _tie() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "---L")
}
case class _timer() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _tostring() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.StringType)
}
case class _turtlecode() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--",
      canBeConcise = false)
}
case class _untie() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "---L")
}
case class _updatemonitor() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType),
      agentClassString = "O---")
}
case class _uphill() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.ReferenceType),
      agentClassString = "-T--")
}
case class _uphill4() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.ReferenceType),
      agentClassString = "-T--")
}
case class _userdirectory() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType | Syntax.BooleanType)
}
case class _userfile() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType | Syntax.BooleanType)
}
case class _userinput() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.StringType)
}
case class _usermessage() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _usernewfile() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType | Syntax.BooleanType)
}
case class _useroneof() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType, Syntax.ListType),
      ret = Syntax.WildcardType)
}
case class _useryesorno() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _wait() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType))
}
case class _watchme() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-TPL")
}
case class _worldheight() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _worldwidth() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
}
case class _wrapcolor() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
//scalastyle:on number.of.types
//scalastyle:on file.size.limit
