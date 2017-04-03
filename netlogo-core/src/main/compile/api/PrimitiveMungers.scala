// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

import org.nlogo.core.Instantiator
import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Command, Instruction, Reporter }

// these classes are used to write optimizers
private class MatchFailedException extends Exception

trait OptimizeMunger[-A <: AstNode, +B <: Instruction] {
  def munge(stmt: A)
  val clazz: Class[_ <: B]
}

trait CommandMunger extends OptimizeMunger[Statement, Command]
trait ReporterMunger extends OptimizeMunger[ReporterApp, Reporter]

trait RewritingCommandMunger extends CommandMunger {
  def munge(stmt: Statement) {
    try munge(new Match(stmt))
    catch { case _: MatchFailedException => }
  }
  def munge(root: Match)
}

trait RewritingReporterMunger extends ReporterMunger {
  def munge(app: ReporterApp) {
    try munge(new Match(app))
    catch { case _: MatchFailedException => }
  }
  def munge(root: Match)
}

class Match(val node: AstNode) {
  def matchit(theClass: Class[_ <: Instruction]) =
    node match {
      case app: ReporterApp if theClass.isInstance(app.reporter) => this
      case stmt: Statement if theClass.isInstance(stmt.command) => this
      case _ => throw new MatchFailedException
    }
  def command =
    node match {
      case stmt: Statement => stmt.command
      case _ => throw new MatchFailedException
    }
  def reporter =
    node match {
      case app: ReporterApp => app.reporter
      case _ => throw new MatchFailedException
    }
  def matchEmptyCommandBlockIsLastArg =
    node match {
      case stmt: Statement if !stmt.args.isEmpty =>
        stmt.args.last match {
          case block: CommandBlock if block.statements.stmts.isEmpty => new Match(block)
          case _ => throw new MatchFailedException
        }
      case _ => throw new MatchFailedException
    }
  def matchArg(index: Int) = {
    val args = node match {
      case stmt: Statement => stmt.args
      case app: ReporterApp => app.args
      case _ => throw new MatchFailedException
    }
    if(index >= args.size) throw new MatchFailedException
    args(index) match {
      case app: ReporterApp => new Match(app)
      case block: ReporterBlock => new Match(block)
      case _ => throw new MatchFailedException
    }
  }
  def matchArg(index: Int, classes: Class[_ <: Instruction]*) = {
    val args = node match {
      case stmt: Statement => stmt.args
      case app: ReporterApp => app.args
      case _ => throw new MatchFailedException
    }
    if(index >= args.size) throw new MatchFailedException
    args(index) match {
      case app: ReporterApp if classes.exists(_.isInstance(app.reporter)) => new Match(app)
      case _ => throw new MatchFailedException
    }
  }
  def matchReporterBlock() = {
    node match {
      case block: ReporterBlock => new Match(block.app)
      case _ => throw new MatchFailedException
    }
  }
  def matchOneArg(theClass: Class[_ <: Instruction]) = {
    try matchArg(0, theClass)
    catch { case _: MatchFailedException => matchArg(1, theClass) }
  }
  def matchOtherArg(alreadyMatched: Match, classes: Class[_ <: Instruction]*): Match = {
    val result: Match =
      try matchArg(0, classes: _*)
      catch { case _: MatchFailedException => return matchArg(1, classes: _*) }
      if(result.node eq alreadyMatched.node) matchArg(1, classes: _*)
      else result
  }
  def strip() {
    node match {
      case app: ReporterApp =>
        while(!app.args.isEmpty) app.removeArgument(0)
      case stmt: Statement =>
        while(!stmt.args.isEmpty) stmt.removeArgument(0)
    }
  }
  def graftArg(newArg: Match) {
    node match {
      case app: ReporterApp => app.addArgument(newArg.node.asInstanceOf[Expression])
      case stmt: Statement => stmt.addArgument(newArg.node.asInstanceOf[Expression])
    }
  }
  def removeLastArg() {
    node match {
      case app: ReporterApp => app.removeArgument(app.args.size - 1)
      case stmt: Statement => stmt.removeArgument(stmt.args.size - 1)
    }
  }

  def replace(newGuy: Instruction) {
    node match {
      case app: ReporterApp =>
        newGuy.copyMetadataFrom(app.reporter)
        app.reporter = newGuy.asInstanceOf[Reporter]
      case stmt: Statement =>
        newGuy.copyMetadataFrom(stmt.command)
        stmt.command = newGuy.asInstanceOf[Command]
    }
  }

  def replace(theClass: Class[_ <: Instruction], constructorArgs: Any*) {
    val newGuy = Instantiator.newInstance[Instruction](theClass, constructorArgs: _*)
    node match {
      case app: ReporterApp =>
        newGuy.copyMetadataFrom(app.reporter)
        app.reporter = newGuy.asInstanceOf[Reporter]
      case stmt: Statement =>
        newGuy.copyMetadataFrom(stmt.command)
        stmt.command = newGuy.asInstanceOf[Command]
    }
  }

  def addArg(theClass: Class[_ <: Reporter], original: ReporterApp): Match = {
    val newGuy = Instantiator.newInstance[Reporter](theClass)
    newGuy.copyMetadataFrom(original.reporter)
    val result = new Match(new ReporterApp(original.coreReporter, newGuy, original.sourceLocation))
    graftArg(result)
    result
  }
}
