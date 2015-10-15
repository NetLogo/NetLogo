// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import utest._
import org.nlogo.core.CompilerException
import org.nlogo.core

// This is where ExpressionParser gets most of its testing.  (It's a lot easier to test it as part
// of the overall front end than it would be to test in strict isolation.)

object FrontEndTests extends TestSuite {

  def tests = TestSuite {
    val PREAMBLE = "to __test "
    val POSTAMBLE = "\nend"

    /// helpers
    def compile(source: String, preamble: String = PREAMBLE): Seq[core.Statements] =
      FrontEnd.frontEnd(preamble + source + POSTAMBLE) match {
        case (procs, _) =>
          procs.map(_.statements)
      }

    /**
     * utility method useful for testing that start()
     * and end() return right answers everywhere
     */
    def statementsToString(ss: Seq[core.Statements], source: String) =
      (for (stmts <- ss) yield {
        val visitor = new PositionsCheckVisitor(source)
        visitor.visitStatements(stmts)
        visitor.buf.toString
      }).mkString

    /// helper
    def testStartAndEnd(source: String, preorderDump: String) {
      assert(preorderDump == statementsToString(compile(source), source))
    }

    // preorder traversal
    class PositionsCheckVisitor(source: String) extends core.AstVisitor {
      val buf = new StringBuilder()
      override def visitCommandBlock(node: core.CommandBlock) { visit(node); super.visitCommandBlock(node) }
      override def visitReporterApp(node: core.ReporterApp) { visit(node); super.visitReporterApp(node) }
      override def visitReporterBlock(node: core.ReporterBlock) { visit(node); super.visitReporterBlock(node) }
      override def visitStatement(node: core.Statement) { visit(node); super.visitStatement(node) }
      override def visitStatements(node: core.Statements) {
        if (node.stmts.isEmpty)
          buf.append(s"${node.getClass.getSimpleName} '' ")
        else visit(node)
        super.visitStatements(node)
      }
      def visit(node: core.AstNode) {
        val start = node.start - PREAMBLE.length
        val end = node.end - PREAMBLE.length
        val text =
          try s"'${source.substring(start, end)}'"
          catch {
            case _: StringIndexOutOfBoundsException => s"out of bounds: ${(start, end)}"
          }
        buf.append(s"${node.getClass.getSimpleName} $text ")
      }
    }

    def runTest(input: String, expectedResult: String, preamble: String = PREAMBLE) {
      val actualResult = compile(input, preamble).mkString
      assert(expectedResult == actualResult)
    }

    def runFailure(input: String, message: String, start: Int, end: Int) {
      doFailure(input, message, start, end)
    }

    def doFailure(input: String, message: String, start: Int, end: Int) {
      val e = intercept[CompilerException] { compile(input) }
      assert(message == e.getMessage)
      assert(start + PREAMBLE.length == e.start)
      assert(end + PREAMBLE.length == e.end)
    }

    /// now, the actual tests
    "DoParseSimpleCommand"-{
      runTest("__ignore round 0.5", "_ignore()[_round()[_const(0.5)[]]]")
    }

    "DoParseCommandWithInfix"-{
      runTest("__ignore 5 + 2", "_ignore()[_plus()[_const(5)[], _const(2)[]]]")
    }

    "DoParseTwoCommands"-{
      runTest("__ignore round 0.5 fd 5",
        "_ignore()[_round()[_const(0.5)[]]] " +
          "_fd()[_const(5)[]]")
    }

    "DoParseBadCommand1"-{
      runFailure("__ignore 1 2 3", "Expected command.", 11, 12)
    }

    "DoParseBadCommand2"-{
      runFailure("__ignore", "__IGNORE expected 1 input.", 0, 8)
    }

    "DoParseReporterOnly"-{
      runFailure("round 1.2", "Expected command.", 0, 5)
    }

    "WrongArgumentType"-{
      runFailure("__ignore count 0", "COUNT expected this input to be an agentset, but got a number instead", 15, 16)
    }

    "missingCloseBracket"-{
      // You could argue that it ought to point to the second bracket and not the first, but this is
      // fine. - ST 1/29
      runFailure("crt 10 [ [", "No closing bracket for this open bracket.", 7, 8)
    }

    "missing name after let"-{
      // here the error is at TokenType.Eof - ST 9/29/14
      runFailure("let", "Expected variable name here",
        core.Token.Eof.start - PREAMBLE.size,
        core.Token.Eof.end - PREAMBLE.size)
    }

    // https://github.com/NetLogo/NetLogo/issues/348
    "let of task variable"-{
      runFailure("foreach [1] [ let ? 0 ]",
        "Names beginning with ? are reserved for use as task inputs",
        18, 19)
    }

    "DoParseMap"-{
      runTest("__ignore map [round ?] [1.2 1.7 3.2]",
        "_ignore()[_map()[_reportertask(1)[_round()[_taskvariable(1)[]]], _const([1.2, 1.7, 3.2])[]]]")
    }

    "DoParseMapShortSyntax"-{
      runTest("__ignore map round [1.2 1.7 3.2]",
        "_ignore()[_map()[_reportertask(1)[_round()[_taskvariable(1)[]]], _const([1.2, 1.7, 3.2])[]]]")
    }

    "DoParseForeach"-{
      runTest("foreach [1 2 3] [__ignore ?]",
        "_foreach()[_const([1, 2, 3])[], _commandtask(1)[[_ignore()[_taskvariable(1)[]]]]]")
    }

    "DoParselet"-{
      runTest("let x 5 __ignore x",
        "_let(Let(X))[_const(5)[]] _ignore()[_letvariable(Let(X))[]]")
    }

    "DoParseParenthesizedCommand"-{
      runTest("(__ignore 5)",
        "_ignore()[_const(5)[]]")
    }

    "DoParseParenthesizedCommandAsFromEvaluator"-{
      runTest("__observercode (__ignore 5) __done",
        "_observercode()[] " +
          "_ignore()[_const(5)[]] " +
          "_done()[]")
    }

    "ParseExpressionWithInfix"-{
      runTest("__ignore 5 + 2",
        "_ignore()[_plus()[_const(5)[], _const(2)[]]]")
    }

    "ParseExpressionWithInfix2"-{
      runTest("__ignore 5 + 2 * 7",
        "_ignore()[_plus()[_const(5)[], _mult()[_const(2)[], _const(7)[]]]]")
    }

    "ParseExpressionWithInfix3"-{
      runTest("__ignore 5 + 2 * 7 - 2",
        "_ignore()[_minus()[_plus()[_const(5)[], _mult()[_const(2)[], _const(7)[]]], _const(2)[]]]")
    }

    "ParseExpressionWithInfixAndPrefix"-{
      runTest("__ignore round 5.2 + log 64 2 * log 64 2 - random 2",
        "_ignore()[_minus()[_plus()[_round()[_const(5.2)[]], _mult()[_log()[_const(64)[], _const(2)[]], _log()[_const(64)[], _const(2)[]]]], _random()[_const(2)[]]]]")
    }

    "ParseConstantInteger"-{
      runTest("__ignore 5",
        "_ignore()[_const(5)[]]")
    }

    "ParseConstantList"-{
      runTest("__ignore [5]",
        "_ignore()[_const([5])[]]")
    }

    "ParseConstantListWithSublists"-{
      runTest("__ignore [[1] [2]]",
        "_ignore()[_const([[1], [2]])[]]")
    }

    "ParseConstantListInsideTask1"-{
      runTest("__ignore n-values 10 [[]]",
        "_ignore()[_nvalues()[_const(10)[], _reportertask(0)[_const([])[]]]]")
    }

    "ParseConstantListInsideTask2"-{
      runTest("__ignore n-values 10 [[5]]",
        "_ignore()[_nvalues()[_const(10)[], _reportertask(0)[_const([5])[]]]]")
    }

    "ParseCommandTask1"-{
      runTest("__ignore task [print ?]",
        "_ignore()[_commandtask(1)[[_print()[_taskvariable(1)[]]]]]")
    }

    "ParseCommandTask2"-{
      runTest("__ignore task [print 5]",
        "_ignore()[_commandtask(0)[[_print()[_const(5)[]]]]]")
    }

    "ParseCommandTask3"-{
      // it would be nice if this resulted in a CompilerException instead
      // of failing at runtime - ST 2/6/11
      runTest("__ignore runresult task [__ignore 5]",
        "_ignore()[_runresult()[_commandtask(0)[[_ignore()[_const(5)[]]]]]]")
    }

    "ParseDiffuse"-{
      runTest("diffuse pcolor 1",
        "_diffuse()[_patchvariable(2)[], _const(1)[]]")
    }

    // in SetBreed2, we are checking that since no singular form of `fish`
    // is provided and it defaults to `turtle`, that the primitive `turtle`
    // isn't mistaken for a singular form and parsed as `_breedsingular` - ST 4/12/14
    "SetBreed1"-{
      runTest("__ignore turtle 0",
        "_ignore()[_turtle()[_const(0)[]]]")
    }

    "SetBreed2"-{
      runTest("__ignore turtle 0",
        "_ignore()[_turtle()[_const(0)[]]]")
    }

    /// tests using testStartAndEnd
    "StartAndEndPositions0"-{
      testStartAndEnd("ca",
        "Statements 'ca' " +
          "Statement 'ca' ")
    }

    "StartAndEndPositions1"-{
      testStartAndEnd("__ignore 5",
        "Statements '__ignore 5' " +
          "Statement '__ignore 5' " +
          "ReporterApp '5' ")
    }

    "StartAndEndPositions2"-{
      testStartAndEnd("__ignore n-values 5 [world-width]",
        "Statements '__ignore n-values 5 [world-width]' " +
          "Statement '__ignore n-values 5 [world-width]' " +
          "ReporterApp 'n-values 5 [world-width]' " +
          "ReporterApp '5' " +
          "ReporterApp '[world-width]' " +
          "ReporterApp 'world-width' ")
    }

    "StartAndEndPositions8"-{
      testStartAndEnd("crt 1",
        "Statements 'crt 1' " +
          "Statement 'crt 1' " +
          "ReporterApp '1' " +
          "CommandBlock '' " +
          "Statements '' ")
    }

    "StartAndEndPositions9"-{
      testStartAndEnd("crt 1 [ ]",
        "Statements 'crt 1 [ ]' " +
          "Statement 'crt 1 [ ]' " +
          "ReporterApp '1' " +
          "CommandBlock '[ ]' " +
          "Statements '' ")
    }

    "StartAndEndPositions10"-{
      testStartAndEnd("ask turtles with [color = red ] [ fd 1 ]",
        "Statements 'ask turtles with [color = red ] [ fd 1 ]' " +
          "Statement 'ask turtles with [color = red ] [ fd 1 ]' " +
          "ReporterApp 'turtles with [color = red ]' " +
          "ReporterApp 'turtles' " +
          "ReporterBlock '[color = red ]' " +
          "ReporterApp 'color = red' " +
          "ReporterApp 'color' " +
          "ReporterApp 'red' " +
          "CommandBlock '[ fd 1 ]' " +
          "Statements 'fd 1' " +
          "Statement 'fd 1' " +
          "ReporterApp '1' ")
    }

    "While"-{
      testStartAndEnd("while [count turtles < 10] [ crt 1 ]",
        "Statements 'while [count turtles < 10] [ crt 1 ]' " +
          "Statement 'while [count turtles < 10] [ crt 1 ]' " +
          "ReporterBlock '[count turtles < 10]' " +
          "ReporterApp 'count turtles < 10' " +
          "ReporterApp 'count turtles' " +
          "ReporterApp 'turtles' " +
          "ReporterApp '10' " +
          "CommandBlock '[ crt 1 ]' " +
          "Statements 'crt 1' " +
          "Statement 'crt 1' " +
          "ReporterApp '1' " +
          "CommandBlock '' " +
          "Statements '' ")
    }

    // issue #417 (source positions for literal lists)
    "literal list"-{
      testStartAndEnd("print [1 2 3]",
        "Statements 'print [1 2 3]' " +
          "Statement 'print [1 2 3]' " +
          "ReporterApp '[1 2 3]' ")
    }


    // Breed reporter tests
    "test breed reporters"-{
      assert(
        compile("ask as [fd 1]", preamble = "breed [as a] to foo ").mkString ==
          "_ask()[_breed(AS)[], [_fd()[_const(1)[]]]]")
    }

    /// duplicate name tests
    def duplicateName(s: String, err: String) = {
      val e = intercept[CompilerException] {
        FrontEnd.frontEnd(s)
      }
      assert(err == e.getMessage)
    }

    "LetSameNameAsCommandProcedure2"-{
      duplicateName("to b let a 5 end  to a end",
        "There is already a procedure called A")
    }

    "LetSameNameAsReporterProcedure2"-{
      duplicateName("to b let a 5 end  to-report a end",
        "There is already a procedure called A")
    }

    "LetNameSameAsEnclosingCommandProcedureName"-{
      duplicateName("to bazort let bazort 5 end",
        "There is already a procedure called BAZORT")
    }

    "LetNameSameAsEnclosingReporterProcedureName"-{
      duplicateName("to-report bazort let bazort 5 report bazort end",
        "There is already a procedure called BAZORT")
    }

    "SameLocalVariableTwice1"-{
      duplicateName("to a1 locals [b b] end",
        "Nothing named LOCALS has been defined.")
    }

    "SameLocalVariableTwice2"-{
      duplicateName("to a2 [b b] end",
        "There is already a local variable called B here")
    }

    "SameLocalVariableTwice3"-{
      duplicateName("to a3 let b 5 let b 6 end",
        "There is already a local variable here called B")
    }

    "SameLocalVariableTwice4"-{
      duplicateName("to a4 locals [b] let b 5 end",
        "Nothing named LOCALS has been defined.")
    }

    "SameLocalVariableTwice5"-{
      duplicateName("to a5 [b] locals [b] end",
        "Nothing named LOCALS has been defined.")
    }

    "SameLocalVariableTwice6"-{
      duplicateName("to a6 [b] let b 5 end",
        "There is already a local variable here called B")
    }
  }
}
