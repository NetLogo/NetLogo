// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

import org.nlogo.core.Instantiator
import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Command, Instruction, Reporter }

// these classes are used to write optimizers
private class MatchFailedException extends Exception

trait OptimizeMunger[-A <: AstNode, +B <: Instruction] {
  def munge(stmt: A): Unit
  val clazz: Class[_ <: B]
}

trait CommandMunger extends OptimizeMunger[Statement, Command]
trait ReporterMunger extends OptimizeMunger[ReporterApp, Reporter]

trait RewritingCommandMunger extends CommandMunger {
  def munge(stmt: Statement): Unit = {
    try munge(Match(stmt))
    catch { case _: MatchFailedException => }
  }
  def munge(root: Match): Unit
}

trait RewritingReporterMunger extends ReporterMunger {
  def munge(app: ReporterApp): Unit = {
    try munge(Match(app))
    catch { case _: MatchFailedException => }
  }
  def munge(root: Match): Unit
}

object Match {
  def apply(node: AstNode): Match = {
    node match {
      case app: ReporterApp => new ReporterAppMatch(app)
      case stmt: Statement => new CommandMatch(stmt)
      case block => new BlockMatch(block)
    }
  }
}

trait Match {
  val node: AstNode

  def matchit(theClass: Class[_ <: Instruction]): Match = throw new MatchFailedException

  def command: Command = throw new MatchFailedException

  def reporter: Reporter = throw new MatchFailedException

  def matchEmptyCommandBlockIsLastArg: Match = throw new MatchFailedException

  def matchArg(index: Int): Match = throw new MatchFailedException

  def matchArg(index: Int, classes: Class[_ <: Instruction]*): Match = throw new MatchFailedException //{

  def matchReporterBlock(): Match = throw new MatchFailedException

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

  def report =
    try node.asInstanceOf[ReporterApp].reporter.report(null)
    catch { case ex: LogoException => throw new IllegalStateException(ex) }

  def strip(): Unit = throw new MatchFailedException

  def graftArg(newArg: Match): Unit = throw new MatchFailedException

  def removeLastArg(): Unit = throw new MatchFailedException

  def replace(newGuy: Instruction): Unit = throw new MatchFailedException

  def replace(theClass: Class[_ <: Instruction], constructorArgs: Any*): Unit = throw new MatchFailedException

  def addArg(theClass: Class[_ <: Reporter], original: ReporterApp): Match = {
    val newGuy = Instantiator.newInstance[Reporter](theClass)
    newGuy.copyMetadataFrom(original.reporter)
    val result = Match(new ReporterApp(original.coreReporter, newGuy, original.sourceLocation))
    graftArg(result)
    result
  }
}

class CommandMatch(val node: Statement) extends Match {

  override def command = node.command

  override def matchEmptyCommandBlockIsLastArg = {
    node.args.last match {
      case block: CommandBlock if block.statements.stmts.isEmpty => Match(block)
      case _ => throw new MatchFailedException
    }
  }

  override def matchit(theClass: Class[_ <: Instruction]) =
    if (theClass.isInstance(node.command)) this
    else throw new MatchFailedException

  override def matchArg(index: Int) = {
    val args = node.args
    if (index >= args.size) throw new MatchFailedException
    args(index) match {
      case app: ReporterApp => Match(app)
      case block: ReporterBlock => Match(block)
      case _ => throw new MatchFailedException
    }
  }

  override def matchArg(index: Int, classes: Class[_ <: Instruction]*) = {
    val args = node.args
    if (index >= args.size) throw new MatchFailedException
    args(index) match {
      case app: ReporterApp if classes.exists(_.isInstance(app.reporter)) => Match(app)
      case _ => throw new MatchFailedException
    }
  }

  override def strip(): Unit = {
    while (!node.args.isEmpty) node.removeArgument(0)
  }

  override def graftArg(newArg: Match): Unit = {
    node.addArgument(newArg.node.asInstanceOf[Expression])
  }

  override def removeLastArg(): Unit = {
    node.removeArgument(node.args.size - 1)
  }

  override def replace(newGuy: Instruction): Unit = {
    newGuy.copyMetadataFrom(node.command)
    node.command = newGuy.asInstanceOf[Command]
  }

  override def replace(theClass: Class[_ <: Instruction], constructorArgs: Any*): Unit = {
    val newGuy = Instantiator.newInstance[Instruction](theClass, constructorArgs: _*)
    newGuy.copyMetadataFrom(node.command)
    node.command = newGuy.asInstanceOf[Command]
  }
}

class ReporterAppMatch(val node: ReporterApp) extends Match {
  override def reporter = node.reporter

  override def matchit(theClass: Class[_ <: Instruction]) =
    if (theClass.isInstance(node.reporter)) this
    else throw new MatchFailedException

  override def matchArg(index: Int) = {
    val args = node.args
    if (index >= args.size) throw new MatchFailedException
    args(index) match {
      case app: ReporterApp => Match(app)
      case block: ReporterBlock => Match(block)
      case _ => throw new MatchFailedException
    }
  }

  override def matchArg(index: Int, classes: Class[_ <: Instruction]*) = {
    val args = node.args
    if (index >= args.size) throw new MatchFailedException
    args(index) match {
      case app: ReporterApp if classes.exists(_.isInstance(app.reporter)) => Match(app)
      case _ => throw new MatchFailedException
    }
  }

  override def strip(): Unit = {
    while(!node.args.isEmpty) node.removeArgument(0)
  }

  override def graftArg(newArg: Match): Unit = {
    node.addArgument(newArg.node.asInstanceOf[Expression])
  }

  override def removeLastArg(): Unit = {
    node.removeArgument(node.args.size - 1)
  }

  override def replace(newGuy: Instruction): Unit = {
    newGuy.copyMetadataFrom(node.reporter)
    node.reporter = newGuy.asInstanceOf[Reporter]
  }

  override def replace(theClass: Class[_ <: Instruction], constructorArgs: Any*): Unit = {
    val newGuy = Instantiator.newInstance[Instruction](theClass, constructorArgs: _*)
    newGuy.copyMetadataFrom(node.reporter)
    node.reporter = newGuy.asInstanceOf[Reporter]
  }
}

class BlockMatch(val node: AstNode) extends Match {
  override def matchReporterBlock() = {
    node match {
      case block: ReporterBlock => Match(block.app)
      case _ => throw new MatchFailedException
    }
  }
}
