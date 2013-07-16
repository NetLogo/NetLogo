// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ parse, nvm, prim }

object Prims {

  object InfixReporter {
    def unapply(r: nvm.Reporter): Option[String] =
      PartialFunction.condOpt(r) {
        case _: prim.etc._plus           => "+"
        case _: prim._minus              => "-"
        case _: prim.etc._mult           => "*"
        case _: prim.etc._div            => "/"
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

  object NormalReporter {
    def unapply(r: nvm.Reporter): Option[String] =
      PartialFunction.condOpt(r) {
        case _: prim.etc._self       => "AgentSet.self"
        case _: prim.etc._patch      => "world.getPatchAt"
        case _: prim.etc._turtles    => "world.turtles"
        case _: prim._patches        => "world.patches"
        case _: prim._count          => "AgentSet.count"
        case _: prim._random         => "Random.nextLong"
        case _: prim.etc._randomxcor => "Prims.randomxcor"
        case _: prim.etc._randomycor => "Prims.randomycor"
      }
  }

  object NormalCommand {
    def unapply(c: nvm.Command): Option[String] =
      PartialFunction.condOpt(c) {
        case _: prim.etc._outputprint      => "println"
        case _: prim.etc._clearall         => "world.clearall"
        case _: prim._createturtles        => "world.createturtles"
        case _: prim._sprout               => "Prims.sprout"
        case _: prim._createorderedturtles => "world.createorderedturtles"
        case _: prim._fd                   => "Prims.fd"
        case _: prim._bk                   => "Prims.bk"
        case _: prim.etc._left             => "Prims.left"
        case _: prim.etc._right            => "Prims.right"
        case _: prim.etc._setxy            => "Prims.setxy"
        case _: prim.etc._die              => "AgentSet.die"
        case _: prim.etc._randomseed       => "Random.setSeed"
      }
  }

  def generateWhile(w: parse.Statement): String = {
    val pred = Compiler.genReporterBlock(w.args.head)
    val body = Compiler.genCommandBlock(w.args.tail.head)
    s"""while ($pred) {
      |$body
      |}""".stripMargin
  }

  def generateIf(s: parse.Statement): String = {
    val pred = Compiler.genReporterApp(s.args.head)
    val body = Compiler.genCommandBlock(s.args.tail.head)
    s"""if ($pred) {
      |$body
      |}""".stripMargin
  }

  def generateIfElse(s: parse.Statement): String = {
    val pred      = Compiler.genReporterApp(s.args.head)
    val thenBlock = Compiler.genCommandBlock(s.args.tail.head)
    val elseBlock = Compiler.genCommandBlock(s.args.tail.tail.head)
    s"""if ($pred) {
      |$thenBlock
      |} else {
      |$elseBlock
      |}""".stripMargin
  }

  def generateAsk(s: parse.Statement): String = {
    val agents = Compiler.genReporterApp(s.args.head)
    val body   = Compiler.genCommandBlock(s.args.tail.head)
    s"AgentSet.ask($agents, ${fun(body)})"
  }

  def fun(body: String) = s"function(){ $body }"

}
