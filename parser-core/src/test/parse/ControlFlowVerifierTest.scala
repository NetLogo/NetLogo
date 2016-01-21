// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ Command, CommandBlock, CompilerException, Expression, FrontEndProcedure, prim,
    ProcedureDefinition, Statement, Statements, StructureDeclarations, Token },
    prim.{ _ask, _carefully, _createturtles => _crt, etc, _fd },
      etc.{ _die, _foreach, _if, _ifelse, _report, _run, _stop }

import
  org.scalacheck.Gen

import
  org.scalatest.{ FunSuite, prop, PropSpec },
    prop.GeneratorDrivenPropertyChecks

class ControlFlowVerifierTest extends FunSuite with GeneratorDrivenPropertyChecks {

  test("no stop in command marks procedure as local-exit-only") {
    forAll(generateProcedure(genNestingPrim, genericPrim)) { pd =>
      val newPd = (new ControlFlowVerifier).visitProcedureDefinition(pd)
      assert(! newPd.nonLocalExit)
    }
  }

  test("stop in command marks procedure as non-local exit") {
    forAll(generateProcedure(genNestingPrim, commandNonLocalExits)) { pd =>
      val newPd = (new ControlFlowVerifier).visitProcedureDefinition(pd)
      assert(newPd.nonLocalExit)
      assert(newPd.statements.nonLocalExit)
      assert(! newPd.statements.stmts.head
        .args.headOption.map(_.asInstanceOf[CommandBlock].statements.nonLocalExit)
        .getOrElse(false))
    }
  }

  test("stop in context does not mark procedure as non-local exit") {
    forAll(nestedStatements(genContextPrim, commandNonLocalExits).map(procdef(false))) { pd =>
      val newPd = (new ControlFlowVerifier).visitProcedureDefinition(pd)
      assert(! newPd.nonLocalExit)
    }
  }

  test("reporter procedures are non-local exit") {
    forAll(generateProcedure(genNestingPrim, Gen.const(_report()), true)) { pd =>
      val newPd = (new ControlFlowVerifier).visitProcedureDefinition(pd)
      assert(newPd.nonLocalExit)
      assert(newPd.statements.nonLocalExit)
    }
  }

  def stmt(prim: Command, args: Expression*) =
    new Statement(prim, 0, 0, "foo.nlogo", args)

  val genNestingPrim: Gen[Command] =
    Gen.oneOf(_if(), _ifelse(), _foreach())

  val genContextPrim: Gen[Command] =
    Gen.oneOf(_ask(), _carefully())

  //prims that are none of nesting, non-local-exit, nor context-creating
  val genericPrim: Gen[Command] =
    Gen.oneOf(_die(), _crt("TURTLE"), _fd())

  val commandNonLocalExits =
    Gen.oneOf(_stop(), _run())

  def statements(stmts: Seq[Statement]): Statements =
    new Statements("abc").copy(stmts = stmts)

  def generateProcedure(ctxPrimGen: Gen[Command], nestedPrimGen: Gen[Command], isReporter: Boolean = false): Gen[ProcedureDefinition] =
    Gen.oneOf(
      nestedPrimGen.map(p => statements(Seq(stmt(p)))),
      nestedStatements(ctxPrimGen, nestedPrimGen))
        .map(procdef(isReporter))

  def nestedStatements(ctxPrimGen: Gen[Command], nestedPrimGen: Gen[Command]): Gen[Statements] = {
    def commandBlock(stmts: Statements): CommandBlock =
      new CommandBlock(stmts, 0, 0, "abc")

    def inBlockContext(ctxPrim: Command, block: Statements): Statements =
      statements(Seq(stmt(ctxPrim, commandBlock(block))))

    for {
      ctxPrim <- ctxPrimGen
      nPrim   <- nestedPrimGen
    } yield inBlockContext(ctxPrim, statements(Seq(stmt(nPrim))))
  }

  def procdef(isReporter: Boolean)(statements: Statements): ProcedureDefinition =
    new ProcedureDefinition(frontEndProcedure(isReporter), statements)

  def frontEndProcedure(reporterProcedure: Boolean): FrontEndProcedure = new FrontEndProcedure {
    def procedureDeclaration: StructureDeclarations.Procedure = ???
    def name: String = "foobar"
    def isReporter: Boolean = reporterProcedure
    def displayName: String = "foobar"
    def filename: String = "foo.nlogo"
    def nameToken: Token = ???
    def argTokens: Seq[Token] = Seq()
    def dump: String = "TEST FRONTENDPROCEDURE"
  }
}
