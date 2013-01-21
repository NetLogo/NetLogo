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
    val pred = genReporterBlock(w.args.head)
    val body = genCommandBlock(w.args.tail.head)
    s"""while ($pred) {
      |$body
      |}""".stripMargin
  }

  def generateIf(s: compiler.Statement): String = {
    val pred = genReporterApp(s.args.head)
    val body = genCommandBlock(s.args.tail.head)
    s"""if ($pred) {
      |$body
      |}""".stripMargin
  }

  def generateIfElse(s: compiler.Statement): String = {
    val pred      = genReporterApp(s.args.head)
    val thenBlock = genCommandBlock(s.args.tail.head)
    val elseBlock = genCommandBlock(s.args.tail.tail.head)
    s"""if ($pred) {
      |$thenBlock
      |} else {
      |$elseBlock
      |}""".stripMargin
  }

  // these could be merged into one function, genExpression
  // but i think the resulting code wold be confusing and potentially error prone.
  // having different functions for each is more clear.

  // TODO: they might belong in Compiler though?

  private def genReporterApp(e: compiler.Expression) = e match {
    case r: compiler.ReporterApp => Compiler.generateReporter(r)
  }
  private def genReporterBlock(e: compiler.Expression) = e match {
    case r: compiler.ReporterBlock => Compiler.generateReporter(r.app)
  }
  private def genCommandBlock(e: compiler.Expression) = e match {
    case cb: compiler.CommandBlock => Compiler.generateCommands(cb.statements)
  }
}
