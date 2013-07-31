// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.api.{ CompilerException, DummyExtensionManager, Program }
import org.nlogo.nvm

class ExpressionParserTests extends FunSuite {

  val PREAMBLE = "to __test "
  val POSTAMBLE = "\nend"

  /// helpers
  def compile(source: String): Seq[Statements] =
    Parser.frontEnd(PREAMBLE + source + POSTAMBLE) match {
      case (procs, _) =>
        procs.map(_.statements)
    }

  /**
   * utility method useful for testing that start()
   * and end() return right answers everywhere
   */
  def statementsToString(ss: Seq[Statements], source: String) =
    (for (stmts <- ss) yield {
      val visitor = new PositionsCheckVisitor(source)
      visitor.visitStatements(stmts)
      visitor.buf.toString
    }).mkString
  /// helper
  def testStartAndEnd(source: String, preorderDump: String) {
    assertResult(preorderDump)(statementsToString(compile(source), source))
  }
  // preorder traversal
  class PositionsCheckVisitor(source: String) extends DefaultAstVisitor {
    val buf = new StringBuilder()
    override def visitCommandBlock(node: CommandBlock) { visit(node); super.visitCommandBlock(node) }
    override def visitReporterApp(node: ReporterApp) { visit(node); super.visitReporterApp(node) }
    override def visitReporterBlock(node: ReporterBlock) { visit(node); super.visitReporterBlock(node) }
    override def visitStatement(node: Statement) { visit(node); super.visitStatement(node) }
    override def visitStatements(node: Statements) {
      if (node.size == 0) buf.append(node.getClass.getSimpleName + " '' ")
      else visit(node)
      super.visitStatements(node)
    }
    def visit(node: AstNode) {
      val start = node.start - PREAMBLE.length
      val end = node.end - PREAMBLE.length
      val text = try { "'" + source.substring(start, end) + "'" }
      catch { case ex: StringIndexOutOfBoundsException => "out of bounds: " + ((start, end)) }
      buf.append(node.getClass.getSimpleName + " " + text + " ")
    }
  }

  def runTest(input: String, result: String) {
    assertResult(result)(compile(input).mkString)
  }
  def runFailure(input: String, message: String, start: Int, end: Int) {
    doFailure(input, message, start, end)
  }
  def doFailure(input: String, message: String, start: Int, end: Int) {
    val e = intercept[CompilerException] { compile(input) }
    assertResult(message)(e.getMessage)
    assertResult(start + PREAMBLE.length())(e.start)
    assertResult(end + PREAMBLE.length())(e.end)
  }

  /// now, the actual tests
  test("testDoParseSimpleCommand") {
    runTest("__ignore round 0.5", "_ignore[_round[_constdouble:0.5[]]]")
  }
  test("testDoParseCommandWithInfix") {
    runTest("__ignore 5 + 2", "_ignore[_plus[_constdouble:5.0[], _constdouble:2.0[]]]")
  }
  test("testDoParseTwoCommands") {
    runTest("__ignore round 0.5 fd 5",
      "_ignore[_round[_constdouble:0.5[]]] " +
      "_fd[_constdouble:5.0[]]")
  }
  test("testDoParseBadCommand1") {
    runFailure("__ignore 1 2 3", "Expected command.", 11, 12)
  }
  test("testDoParseBadCommand2") {
    runFailure("__ignore", "__IGNORE expected 1 input.", 0, 8)
  }
  test("testDoParseReporterOnly") {
    runFailure("round 1.2", "Expected command.", 0, 5)
  }
  test("testWrongArgumentType") {
    runFailure("__ignore count 0", "COUNT expected this input to be an agentset, but got a number instead", 15, 16)
  }
  test("missingCloseBracket") {
    // You could argue that it ought to point to the second bracket and not the first, but this is
    // fine. - ST 1/22/09
    runFailure("crt 10 [ [", "No closing bracket for this open bracket.", 7, 8)
  }
  test("testDoParseMap") {
    runTest("__ignore map [round ?] [1.2 1.7 3.2]",
      "_ignore[_map[_reportertask[_round[_taskvariable:1[]]], _constlist:[1.2 1.7 3.2][]]]")
  }
  test("testDoParseMapShortSyntax") {
    runTest("__ignore map round [1.2 1.7 3.2]",
      "_ignore[_map[_reportertask[_round[_taskvariable:1[]]], _constlist:[1.2 1.7 3.2][]]]")
  }
  test("testDoParseForeach") {
    runTest("foreach [1 2 3] [__ignore ?]",
      "_foreach[_constlist:[1 2 3][], _commandtask:(command task from: procedure __TEST)[]]" +
      "_ignore[_taskvariable:1[]]")
  }
  test("testDoParseParenthesizedCommand") {
    runTest("(__ignore 5)", "_ignore[_constdouble:5.0[]]")
  }
  test("testDoParseParenthesizedCommandAsFromEvaluator") {
    runTest("__observercode (__ignore 5) __done",
      "_observercode[] _ignore[_constdouble:5.0[]] _done[]")
  }
  test("testParseExpressionWithInfix") {
    runTest("__ignore 5 + 2",
      "_ignore[_plus[_constdouble:5.0[], _constdouble:2.0[]]]")
  }
  test("testParseExpressionWithInfix2") {
    runTest("__ignore 5 + 2 * 7",
      "_ignore[_plus[_constdouble:5.0[], _mult[_constdouble:2.0[], _constdouble:7.0[]]]]")
  }
  test("testParseExpressionWithInfix3") {
    runTest("__ignore 5 + 2 * 7 - 2",
      "_ignore[_minus[_plus[_constdouble:5.0[], _mult[_constdouble:2.0[], _constdouble:7.0[]]], _constdouble:2.0[]]]")
  }
  test("testParseExpressionWithInfixAndPrefix") {
    runTest("__ignore round 5.2 + log 64 2 * log 64 2 - random 2",
      "_ignore[_minus[_plus[_round[_constdouble:5.2[]], _mult[_log[_constdouble:64.0[], _constdouble:2.0[]], _log[_constdouble:64.0[], _constdouble:2.0[]]]], _random[_constdouble:2.0[]]]]")
  }
  test("testParseConstantInteger") {
    runTest("__ignore 5", "_ignore[_constdouble:5.0[]]")
  }
  test("testParseConstantList") {
    runTest("__ignore [5]", "_ignore[_constlist:[5][]]")
  }
  test("testParseConstantListWithSublists") {
    runTest("__ignore [[1] [2]]", "_ignore[_constlist:[[1] [2]][]]")
  }
  test("testParseConstantListInsideTask1") {
    runTest("__ignore n-values 10 [[]]",
      "_ignore[_nvalues[_constdouble:10.0[], _reportertask[_constlist:[][]]]]")
  }
  test("testParseConstantListInsideTask2") {
    runTest("__ignore n-values 10 [[5]]",
      "_ignore[_nvalues[_constdouble:10.0[], _reportertask[_constlist:[5][]]]]")
  }
  test("testParseCommandTask1") {
    runTest("__ignore task [print ?]",
      "_ignore[_task[_commandtask:(command task from: procedure __TEST)[]]]" +
      "_print[_taskvariable:1[]]")
  }
  test("testParseCommandTask2") {
    runTest("__ignore task [print 5]",
      "_ignore[_task[_commandtask:(command task from: procedure __TEST)[]]]" +
      "_print[_constdouble:5.0[]]")
  }
  test("testParseCommandTask3") {
    // it would be nice if this resulted in a CompilerException instead
    // of failing at runtime - ST 2/6/11
    runTest("__ignore runresult task [__ignore 5]",
      "_ignore[_runresult[_task[_commandtask:(command task from: procedure __TEST)[]]]]" +
        "_ignore[_constdouble:5.0[]]")
  }
  test("testParseDiffuse") {
    runTest("diffuse pcolor 1",
      "_diffuse[_reference:Patch,2[], _constdouble:1.0[]]")
  }
  /// tests using testStartAndEnd
  test("testStartAndEndPositions0") {
    testStartAndEnd("ca",
      "Statements 'ca' " +
      "Statement 'ca' ")
  }
  test("testStartAndEndPositions1") {
    testStartAndEnd("__ignore 5",
      "Statements '__ignore 5' " +
      "Statement '__ignore 5' " +
      "ReporterApp '5' ")
  }
  test("testStartAndEndPositions2") {
    testStartAndEnd("__ignore n-values 5 [world-width]",
      "Statements '__ignore n-values 5 [world-width]' " +
      "Statement '__ignore n-values 5 [world-width]' " +
      "ReporterApp 'n-values 5 [world-width]' " +
      "ReporterApp '5' " +
      "ReporterApp '[world-width]' " +
      "ReporterApp 'world-width' ")
  }
  test("testStartAndEndPositions8") {
    testStartAndEnd("crt 1",
      "Statements 'crt 1' " +
      "Statement 'crt 1' " +
      "ReporterApp '1' " +
      "CommandBlock '' " +
      "Statements '' ")
  }
  test("testStartAndEndPositions9") {
    testStartAndEnd("crt 1 [ ]",
      "Statements 'crt 1 [ ]' " +
      "Statement 'crt 1 [ ]' " +
      "ReporterApp '1' " +
      "CommandBlock '[ ]' " +
      "Statements '' ")
  }
  test("testWhile") {
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

}
