// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ compiler, nvm, prim }

object Prims {

  object InfixReporter {
    def unapply(r: nvm.Reporter): Option[String] =
      PartialFunction.condOpt(r) {
        case _: prim._plus        => "+"
        case _: prim._minus       => "-"
        case _: prim.etc._mult    => "*"
        case _: prim.etc._div     => "/"
        case _: prim._equal       => "==="
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

  object NormalCommand {
    def unapply(c: nvm.Command): Option[String] =
      PartialFunction.condOpt(c) {
        case _: prim.etc._outputprint      => "println"
        case _: prim.etc._clearall         => "World.clearall"
        case _: prim._createorderedturtles => "World.createorderedturtles"
      }
  }

  def generateWhile(w: compiler.Statement): String = {
    val pred = w.args.head match {
      case r: compiler.ReporterBlock =>
        Compiler.generateReporter(r.app)
    }
    val body = w.args.tail.head match {
      case cb: compiler.CommandBlock =>
        Compiler.generateCommands(cb.statements)
    }
    s"""while ($pred) {
      |$body
      |}""".stripMargin
  }

  def generateIf(s: compiler.Statement): String = {
    val pred = s.args.head match {
      case r: compiler.ReporterApp =>
        Compiler.generateReporter(r)
    }
    val body = s.args.tail.head match {
      case cb: compiler.CommandBlock =>
        Compiler.generateCommands(cb.statements)
    }
    s"""if ($pred) {
      |$body
      |}""".stripMargin
  }

  def generateIfElse(s: compiler.Statement): String = {
    val pred = s.args.head match {
      case r: compiler.ReporterApp =>
        Compiler.generateReporter(r)
    }
    val thenBlock = s.args.tail.head match {
      case cb: compiler.CommandBlock =>
        Compiler.generateCommands(cb.statements)
    }
    val elseBlock = s.args.tail.tail.head match {
      case cb: compiler.CommandBlock =>
        Compiler.generateCommands(cb.statements)
    }
    s"""if ($pred) {
      |$thenBlock
      |} else {
      |$elseBlock
      |}""".stripMargin
  }

}
