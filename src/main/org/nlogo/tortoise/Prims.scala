// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ compile, nvm, prim },
  compile._

object Prims {

  object InfixReporter {
    def unapply(r: nvm.Reporter): Option[String] =
      PartialFunction.condOpt(r) {
        case _: prim.etc._plus           => "+"
        case _: prim._minus              => "-"
        case _: prim.etc._mult           => "*"
        case _: prim.etc._div            => "/"
        case _: prim.etc._mod            => "%"
        case _: prim._equal              => "==="
        case _: prim._notequal           => "!=="
        case _: prim._lessthan           => "<"
        case _: prim._greaterthan        => ">"
        case _: prim.etc._greaterorequal => ">="
        case _: prim.etc._lessorequal    => "<="
        case _: prim._and                => "&&"
        case _: prim._or                 => "||"
      }
  }

  // Scalastyle is right to complain about this gruesomely large match statement,
  // but it isn't worth failing the build over (for the time being) - ST 10/14/13
  // scalastyle:off cyclomatic.complexity
  object NormalReporter {
    def unapply(r: nvm.Reporter): Option[String] =
      PartialFunction.condOpt(r) {
        case _: prim.etc._self        => "AgentSet.self"
        case _: prim.etc._patch       => "world.getPatchAt"
        case _: prim.etc._turtles     => "world.turtles"
        case _: prim._patches         => "world.patches"
        case _: prim.etc._ticks       => "world.ticks"
        case _: prim.etc._timer       => "world.timer"
        case _: prim._count           => "AgentSet.count"
        case _: prim._any             => "AgentSet.any"
        case _: prim._random          => "Random.nextLong"
        case _: prim._list            => "Prims.list"
        case _: prim.etc._first       => "Prims.first"
        case _: prim.etc._last        => "Prims.last"
        case _: prim.etc._fput        => "Prims.fput"
        case _: prim.etc._lput        => "Prims.lput"
        case _: prim.etc._butfirst    => "Prims.butfirst"
        case _: prim.etc._butlast     => "Prims.butlast"
        case _: prim.etc._sort        => "Prims.sort"
        case _: prim.etc._max         => "Prims.max"
        case _: prim.etc._length      => "Prims.length"
        case _: prim.etc._min         => "Prims.min"
        case _: prim.etc._mean        => "Prims.mean"
        case _: prim._sum             => "Prims.sum"
        case _: prim.etc._abs         => "StrictMath.abs"
        case _: prim.etc._randomfloat => "Prims.randomfloat"
        case _: prim.etc._randomxcor  => "Prims.randomxcor"
        case _: prim.etc._randomycor  => "Prims.randomycor"
        case _: prim._oneof           => "AgentSet.oneOf"
        case _: prim.etc._removeduplicates => "Prims.removeDuplicates"
        case _: prim.etc._patchset    => "Prims.patchset"
        case _: prim._not             => "!"
        case _: prim.etc._distance    => "AgentSet.self().distance"
        case _: prim.etc._distancexy  => "AgentSet.self().distancexy"
        case _: prim.etc._patchahead  => "AgentSet.self().patchAhead"
        case _: prim.etc._patchrightandahead  => "AgentSet.self().patchRightAndAhead"
        case _: prim.etc._canmove     => "AgentSet.self().canMove"
        case _: prim.etc._shadeof     => "Prims.shadeOf"
        case _: prim.etc._scalecolor  => "Prims.scaleColor"
        case _: prim.etc._turtleshere => "AgentSet.self().turtlesHere"
        case _: prim.etc._sin         => "Trig.unsquashedSin"
        case _: prim.etc._cos         => "Trig.unsquashedCos"
        case _: prim.etc._floor       => "StrictMath.floor"
        case _: prim.etc._round       => "StrictMath.round"
        case _: prim.etc._turtle      => "world.getTurtle"
      }
  }
  // scalastyle:on cyclomatic.complexity

  object NormalCommand {
    def unapply(c: nvm.Command): Option[String] =
      PartialFunction.condOpt(c) {
        case _: prim.etc._outputprint      => "Prims.outputprint"
        case _: prim.etc._clearall         => "world.clearall"
        case _: prim.etc._clearticks       => "world.clearTicks"
        case _: prim.etc._resizeworld      => "world.resize"
        case _: prim.etc._resetticks       => "world.resetTicks"
        case _: prim.etc._resettimer       => "world.resetTimer"
        case _: prim.etc._tick             => "world.tick"
        case _: prim.etc._tickadvance      => "world.advancetick"
        case _: prim.etc._setdefaultshape  => "Breeds.setDefaultShape"
        case _: prim.etc._moveto           => "AgentSet.self().moveto"
        case _: prim._fd                   => "Prims.fd"
        case _: prim._bk                   => "Prims.bk"
        case _: prim.etc._left             => "Prims.left"
        case _: prim.etc._right            => "Prims.right"
        case _: prim.etc._setxy            => "Prims.setxy"
        case _: prim.etc._die              => "AgentSet.die"
        case _: prim.etc._randomseed       => "Random.setSeed"
        case _: prim.etc._diffuse          => "world.topology().diffuse"
        case _: prim.etc._setcurrentplot   => "noop"
        case _: prim.etc._setcurrentplotpen => "noop"
        case _: prim.etc._plot             => "noop"
      }
  }

  def generateWhile(w: Statement): String = {
    val pred = Compiler.genReporterBlock(w.args.head)
    val body = Compiler.genCommandBlock(w.args.tail.head)
    s"""while ($pred) {
      |$body
      |}""".stripMargin
  }

  def generateIf(s: Statement): String = {
    val pred = Compiler.genReporterApp(s.args.head)
    val body = Compiler.genCommandBlock(s.args.tail.head)
    s"""if ($pred) {
      |$body
      |}""".stripMargin
  }

  def generateIfElse(s: Statement): String = {
    val pred      = Compiler.genReporterApp(s.args.head)
    val thenBlock = Compiler.genCommandBlock(s.args.tail.head)
    val elseBlock = Compiler.genCommandBlock(s.args.tail.tail.head)
    s"""if ($pred) {
      |$thenBlock
      |} else {
      |$elseBlock
      |}""".stripMargin
  }

  def generateAsk(s: Statement, shuffle: Boolean): String = {
    val agents = Compiler.genReporterApp(s.args.head)
    val body   = fun(Compiler.genCommandBlock(s.args.tail.head))
    s"AgentSet.ask($agents, $shuffle, $body);"
  }

  def generateCreateTurtles(s: Statement, ordered: Boolean): String = {
    import org.nlogo.prim._
    val n = Compiler.genReporterApp(s.args.head)
    val name = if (ordered) "createorderedturtles" else "createturtles"
    val breed =
      s.command match {
        case x: _createturtles => x.breedName
        case x: _createorderedturtles => x.breedName
        case x => throw new IllegalArgumentException("How did you get here with class of type " + x.getClass.getName)
      }
    val body = fun(Compiler.genCommandBlock(s.args.tail.head))
    s"""AgentSet.ask(world.$name($n, "$breed"), true, $body);"""
  }

  def generateSprout(s: Statement): String = {
    val n = Compiler.genReporterApp(s.args.head)
    val body = fun(Compiler.genCommandBlock(s.args.tail.head))
    s"AgentSet.ask(Prims.sprout($n), true, $body);"
  }

  def generateHatch(s: Statement, breedName: String): String = {
    val n = Compiler.genReporterApp(s.args.head)
    val body = fun(Compiler.genCommandBlock(s.args.tail.head))
    s"""AgentSet.ask(Prims.hatch($n, "$breedName"), true, $body);"""
  }

  def fun(body: String) = s"function(){ $body }"

}
