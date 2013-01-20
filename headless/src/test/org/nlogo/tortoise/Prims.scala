// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ compiler, nvm, prim }
import compiler.{CommandBlock, ReporterBlock}

object Prims {

  object InfixReporter {
    def unapply(r: nvm.Reporter): Option[String] =
      PartialFunction.condOpt(r) {
        case _: prim._plus        => "+"
        case _: prim._minus       => "-"
        case _: prim.etc._mult    => "*"
        case _: prim.etc._div     => "/"
        case _: prim._lessthan    => "<"
        case _: prim._greaterthan => ">"
      }
  }

  object NormalReporter {
    def unapply(r: nvm.Reporter): Option[String] =
      PartialFunction.condOpt(r) {
        case _: prim._turtles  => "World.turtles"
        case _: prim._count    => "AgentSet.count"
      }
  }

  object SpecialCommand {
    def unapply(c: nvm.Command): Option[String] =
      PartialFunction.condOpt(c) {
        case _: prim._done             => ""
        case _: prim.etc._observercode => ""
        case _: prim.etc._while        => "while"
      }
  }

  object NormalCommand {
    def unapply(c: nvm.Command): Option[String] =
      PartialFunction.condOpt(c) {
        case _: prim.etc._outputprint      => "println"
        case _: prim.etc._clearall         => "World.clearall"
        case _: prim._createorderedturtles => "World.createorderedturtles"
      }
  }

  def generateWhile(w: compiler.Statement): String = {
    val pred = Compiler.generateReporter(w.args.head match {
      case r: ReporterBlock => r.app
    })
    val body = Compiler.generateCommands(w.args.tail.head match {
      case cb: CommandBlock => cb.statements
    })
    s"""while ($pred) {
      |$body
      |}""".stripMargin
  }

}
