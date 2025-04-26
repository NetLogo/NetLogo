// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

import org.nlogo.core.{ Command => CoreCommand, Reporter => CoreReporter,
  SourceLocation, Syntax }
import org.nlogo.core.prim.{ _const => _coreconst }
import org.nlogo.nvm.{ Command, Context, Reporter }
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.Inside

object PrimitiveMungersTests {
  class DummyCoreCommand(rights: Int*) extends CoreCommand {
    def syntax = Syntax.commandSyntax(right = rights.toList)
  }
  class DummyCoreReporter extends CoreReporter {
    def syntax = Syntax.reporterSyntax(ret = Syntax.NumberType)
  }
  class DummyCommand extends Command {
    def perform(c: Context): Unit = {}
  }
  class DummyReporter extends Reporter {
    def report(c: Context): AnyRef = Double.box(0.0)
  }
}

import PrimitiveMungersTests._

class PrimitiveMungersTests extends AnyFunSuite with Inside {

  trait TestCommand {
    val command = new DummyCommand()
  }

  trait TestReporter {
    val testReporter = repApp(new DummyCoreReporter(), new DummyReporter())
  }

  def newMatch(a: AstNode): Match = {
    Match(a)
  }

  val constNum = {
    new ReporterApp(new _coreconst(Double.box(0.0)), new DummyReporter(), Seq(), new SourceLocation(0, 0, ""))
  }

  def statement(cc: CoreCommand, c: Command, args: Expression*): Statement = {
    new Statement(cc, c, args, new SourceLocation(0, 0, ""))
  }

  def repApp(cr: CoreReporter, r: Reporter): ReporterApp = {
    new ReporterApp(cr, r, Seq(), new SourceLocation(0, 0, ""))
  }

  test("match.command returns the statement command") {
    new TestCommand {
      val m = newMatch(statement(new DummyCoreCommand(), command))
      assert(m.command == command)
    }
  }

  test("match.replace replaces the root instruction") {
    new TestCommand {
      val m = newMatch(statement(new DummyCoreCommand(), command))
      val command2 = new DummyCommand()
      m.replace(command2)
      assert(m.command eq command2)
    }
  }

  test("match.strip removes each argument to the node") {
    new TestCommand {
      val m = newMatch(statement(new DummyCoreCommand(), command, constNum))
      m.strip()
      inside(m.node) { case s: Statement => assert(s.args.length == 0) }
    }
  }

  test("matchEmptyCommandBlockIsLastArg matches empty blocks") {
    new TestCommand {
      val emptyBlock = new StatementsBuilder { }
      val m = newMatch(statement(new DummyCoreCommand(Syntax.CommandBlockType), command, emptyBlock.buildBlock))
      m.matchEmptyCommandBlockIsLastArg
    }
  }

  test("matchEmptyCommandBlockIsLastArg fails on non-empty block") {
    new TestCommand {
      val emptyBlock = new StatementsBuilder {
        this.statement(new DummyCoreCommand(), new DummyCommand())
      }
      val m = newMatch(statement(new DummyCoreCommand(Syntax.CommandBlockType), command, emptyBlock.buildBlock))
      intercept[MatchFailedException] {
        m.matchEmptyCommandBlockIsLastArg
      }
    }
  }

  test("matchArg fails when there are no arguments") {
    new TestCommand {
      val m = newMatch(statement(new DummyCoreCommand(), command, constNum))
      m.strip() //no more args
      intercept[MatchFailedException] {
        m.matchArg(0)
      }
    }
  }

  test("matchArg succeeds when there is an argument at its index") {
    new TestCommand with TestReporter {
      val m = newMatch(statement(new DummyCoreCommand(), command, testReporter))
      m.matchArg(0)
    }
  }

  test("matchArg fails when there is no argument at its index") {
    new TestCommand with TestReporter{
      val m = newMatch(statement(new DummyCoreCommand(), command, testReporter))
      intercept[MatchFailedException] {
        m.matchArg(1)
      }
    }
  }

  test("matchArg fails when class is not reporter") {
    new TestCommand with TestReporter {
      val m = newMatch(statement(new DummyCoreCommand(), command, testReporter))
      intercept[MatchFailedException] {
        m.matchArg(0, classOf[DummyCommand])
      }
    }
  }

  test("matchArg passes when class is reporter") {
    new TestCommand with TestReporter {
      val m = newMatch(statement(new DummyCoreCommand(), command, testReporter))
      m.matchArg(0, classOf[DummyReporter])
    }
  }

  test("match.reporter returns the ReporterApp reporter") {
    val reporter = new DummyReporter()
    val m = newMatch(repApp(new DummyCoreReporter(), reporter))
    assert(m.reporter == reporter)
  }

  test("match.replace uses new class arguments") {
    new TestCommand {
      val m = newMatch(statement(new DummyCoreCommand(), command))
      m.replace(classOf[DummyCommand])
    }
  }

  test("addArg passes when argument is added") {
    new TestCommand with TestReporter {
      val m = newMatch(constNum)
      m.addArg(classOf[DummyReporter], testReporter)
    }
  }

  test("graftArg passes when argument is added") {
    new TestCommand with TestReporter {
      val m = newMatch(statement(new DummyCoreCommand(), command, testReporter))
      val m2 = newMatch(statement(new DummyCoreCommand(), command, constNum))
      m.graftArg(m2.matchArg(0))
      assert(m.matchArg(1).reporter == constNum.reporter)
    }
  }

  test("removeLastArg fails when argument is removed") {
    new TestCommand with TestReporter {
      val m = newMatch(statement(new DummyCoreCommand(), command, testReporter))
      m.removeLastArg()
      intercept[MatchFailedException] {
        m.matchArg(0, classOf[DummyCommand])
      }
    }
  }

  test("matchReporterBlock passes when reporter block is matched") {
    new TestCommand with TestReporter {
      val repBlock = new ReporterBuilder { }
      val m = newMatch(repBlock.buildBlock)
      m.matchReporterBlock()
    }
  }
} 
