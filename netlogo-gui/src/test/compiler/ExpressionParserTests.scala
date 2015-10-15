// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.api.{ DummyExtensionManager, ThreeDProgram }
import org.nlogo.core.NetLogoCore
import org.nlogo.core.Program
import org.nlogo.core.CompilerException
import org.nlogo.nvm.Procedure

// Normally we don't bother declaring stuff in test classes private, but sometimes (as a few times
// below) it's necessary in order to avoid "escapes its defining scope" errors. - ST 11/25/08
class ExpressionParserTests extends FunSuite {

  val PREAMBLE = "to __test "
  val POSTAMBLE = "\nend"

  /// helpers
  private def compile(source: String, is3D: Boolean): Seq[Statements] = { // must be private
    val wrappedSource = PREAMBLE + source + POSTAMBLE
    val program = Program.fromDialect(if (is3D) ThreeDProgram else NetLogoCore)
    implicit val tokenizer = if (is3D) Compiler.Tokenizer3D else Compiler.Tokenizer2D
    val results = TestHelper.structureParse(tokenizer.tokenizeAllowingRemovedPrims(wrappedSource), program)
    assertResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    val tokens =
      new IdentifierParser(results.program, java.util.Collections.emptyMap[String, Procedure], results.procedures)
        .process(results.tokens(procedure).iterator, procedure)
    new ExpressionParser(procedure).parse(tokens).map(_.statements)
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
    assertResult(preorderDump)(statementsToString(compile(source, false), source))
    assertResult(preorderDump)(
      statementsToString(compile(source, true), source))
  }
  // preorder traversal
  private class PositionsCheckVisitor(source: String) extends DefaultAstVisitor { // must be private
    val buf = new StringBuilder()
    override def visitCommandBlock(node: CommandBlock) { visit(node); super.visitCommandBlock(node) }
    override def visitReporterApp(node: ReporterApp) { visit(node); super.visitReporterApp(node) }
    override def visitReporterBlock(node: ReporterBlock) { visit(node); super.visitReporterBlock(node) }
    override def visitStatement(node: Statement) { visit(node); super.visitStatement(node) }
    override def visitStatements(node: Statements) {
      if (node.body.size == 0) buf.append(node.getClass.getSimpleName + " '' ")
      else visit(node)
      super.visitStatements(node)
    }
    def visit(node: AstNode) {
      val start = node.start - PREAMBLE.length
      val end = node.end - PREAMBLE.length
      val text = try { "'" + source.substring(start, end) + "'" }
      catch { case ex: StringIndexOutOfBoundsException => "out of bounds: " + (start, end) }
      buf.append(node.getClass.getSimpleName + " " + text + " ")
    }
  }

  def runTest(input: String, result: String) {
    assertResult(result)(compile(input, false).mkString)
    assertResult(result)(compile(input, true).mkString)
  }
  def runTest(input: String, result2D: String, result3D: String) {
    assertResult(result2D)(compile(input, false).mkString)
    assertResult(result3D)(compile(input, true).mkString)
  }
  def runFailure(input: String, message: String, start: Int, end: Int) {
    doFailure(input, message, start, end, false)
    doFailure(input, message, start, end, true)
  }
  def doFailure(input: String, message: String, start: Int, end: Int, is3D: Boolean) {
    val e = intercept[CompilerException] {
      compile(input, is3D)
    }
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
      "_diffuse[_reference:Patch,2[], _constdouble:1.0[]]",
      "_diffuse[_reference:Patch,3[], _constdouble:1.0[]]")
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
  test("testStartAndEndPositions3") {
    testStartAndEnd("print values-from patches [5]",
      "Statements 'print values-from patches [5]' " +
      "Statement 'print values-from patches [5]' " +
      "ReporterApp 'values-from patches [5]' " +
      "ReporterApp 'patches' " +
      "ReporterBlock '[5]' " +
      "ReporterApp '5' ")
  }
  test("testStartAndEndPositions4") {
    testStartAndEnd("print max values-from patches [5]",
      "Statements 'print max values-from patches [5]' " +
      "Statement 'print max values-from patches [5]' " +
      "ReporterApp 'max values-from patches [5]' " +
      "ReporterApp 'values-from patches [5]' " +
      "ReporterApp 'patches' " +
      "ReporterBlock '[5]' " +
      "ReporterApp '5' ")
  }
  test("testStartAndEndPositions5") {
    testStartAndEnd("print max values-from patches [5] > 15",
      "Statements 'print max values-from patches [5] > 15' " +
      "Statement 'print max values-from patches [5] > 15' " +
      "ReporterApp 'max values-from patches [5] > 15' " +
      "ReporterApp 'max values-from patches [5]' " +
      "ReporterApp 'values-from patches [5]' " +
      "ReporterApp 'patches' " +
      "ReporterBlock '[5]' " +
      "ReporterApp '5' " +
      "ReporterApp '15' ")
  }
  test("testStartAndEndPositions6") {
    testStartAndEnd("if max values-from patches [5] > 15 [ ]",
      "Statements 'if max values-from patches [5] > 15 [ ]' " +
      "Statement 'if max values-from patches [5] > 15 [ ]' " +
      "ReporterApp 'max values-from patches [5] > 15' " +
      "ReporterApp 'max values-from patches [5]' " +
      "ReporterApp 'values-from patches [5]' " +
      "ReporterApp 'patches' " +
      "ReporterBlock '[5]' " +
      "ReporterApp '5' " +
      "ReporterApp '15' " +
      "CommandBlock '[ ]' " +
      "Statements '' ")
  }
  test("testStartAndEndPositions7") {
    testStartAndEnd("if max values-from patches [5] > 15 [ die ]",
      "Statements 'if max values-from patches [5] > 15 [ die ]' " +
      "Statement 'if max values-from patches [5] > 15 [ die ]' " +
      "ReporterApp 'max values-from patches [5] > 15' " +
      "ReporterApp 'max values-from patches [5]' " +
      "ReporterApp 'values-from patches [5]' " +
      "ReporterApp 'patches' " +
      "ReporterBlock '[5]' " +
      "ReporterApp '5' " +
      "ReporterApp '15' " +
      "CommandBlock '[ die ]' " +
      "Statements 'die' " +
      "Statement 'die' ")
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
  test("testStartAndEndPositions10") {
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
  // issue #417 (source positions for literal lists)
  test("literal list") {
    testStartAndEnd("print [1 2 3]",
      "Statements 'print [1 2 3]' " +
      "Statement 'print [1 2 3]' " +
      "ReporterApp '[1 2 3]' ")
  }

}
