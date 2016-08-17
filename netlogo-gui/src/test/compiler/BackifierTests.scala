// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.core.{ AgentVariables, Dialect, Command, CompilerException,
  Femto, Instruction, Program, Reporter, Syntax,
  TokenMapperInterface => CoreTokenMapperInterface }
import org.scalatest.FunSuite

class BackifierTests extends FunSuite {
  case class _deadprim() extends Command {
    override def syntax = Syntax.commandSyntax(right = List())
  }

  case class _deadfd() extends Command with ReplacedPrim {
    override def syntax =
      Syntax.commandSyntax(
        right = List(Syntax.NumberType),
        agentClassString = "-T--")
    def recommendedReplacement = "FD"
  }

  class TestMapper extends CoreTokenMapperInterface {
    val commandMap = Map(
      "FD"       -> Femto.get[Command]("org.nlogo.core.prim._fd"),
      "DEADFD"   -> _deadfd(),
      "DEADPRIM" -> _deadprim()
    )
    def getCommand(s: String): Option[Command] = commandMap.get(s)
    def getReporter(s: String): Option[Reporter] = None
    def breedInstruction(primName: String, breedName: String): Option[Instruction] = None

    def allCommandNames: Set[String] = commandMap.keySet
    def allReporterNames: Set[String] = Set()
  }

  class TestDialect extends Dialect {
    val is3D           = false
    val agentVariables = AgentVariables
    val tokenMapper    = new TestMapper()
  }

  def compileError(source: String, expectedError: String): Unit = {
    val program = Program.fromDialect(new TestDialect())
    try {
      TestHelper.compiledProcedures(s"to __test $source \nend", program)
      fail(s"expected compilation of: $source to throw an exception, but it didn't!")
    } catch {
      case e: CompilerException => assert(e.getMessage.contains(expectedError))
      case other: Exception => fail(s"expected compilation of: $source to throw a compiler exception, it threw $other")
    }
  }

  test("errors when primitive has no conversion available") {
    compileError("DEADPRIM", "Nothing named DEADPRIM has been defined")
  }

  test("errors with conversion, when available") {
    compileError("DEADFD 1", "DEADFD is no longer a primitive, use FD instead")
  }
}
