// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core

import org.scalatest.funsuite.AnyFunSuite

class SourcePositionTests extends AnyFunSuite with BaseParserTest {
  /// tests using testStartAndEnd
  test("StartAndEndPositions0") {
    testStartAndEnd("ca",
      "Statements 'ca' " +
      "Statement 'ca' ")
  }
  test("StartAndEndPositions1") {
    testStartAndEnd("__ignore 5",
      "Statements '__ignore 5' " +
      "Statement '__ignore 5' " +
      "ReporterApp '5' ")
  }
  test("StartAndEndPositions2") {
    testStartAndEnd("__ignore n-values 5 [[] -> world-width]",
      "Statements '__ignore n-values 5 [[] -> world-width]' " +
      "Statement '__ignore n-values 5 [[] -> world-width]' " +
      "ReporterApp 'n-values 5 [[] -> world-width]' " +
      "ReporterApp '5' " +
      "ReporterApp '[[] -> world-width]' " +
      "ReporterApp 'world-width' ")
  }
  test("StartAndEndPositions8") {
    testStartAndEnd("crt 1",
      "Statements 'crt 1' " +
      "Statement 'crt 1' " +
      "ReporterApp '1' " +
      "CommandBlock '' " +
      "Statements '' ")
  }
  test("StartAndEndPositions9") {
    testStartAndEnd("crt 1 [ ]",
      "Statements 'crt 1 [ ]' " +
      "Statement 'crt 1 [ ]' " +
      "ReporterApp '1' " +
      "CommandBlock '[ ]' " +
      "Statements '' ")
  }

  test("StartAndEndPositions10") {
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

  test("While") {
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
  test("literal list") {
    testStartAndEnd("print [1 2 3]",
      "Statements 'print [1 2 3]' " +
      "Statement 'print [1 2 3]' " +
      "ReporterApp '[1 2 3]' ")
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
  def testStartAndEnd(source: String, preorderDump: String): Unit = {
    assertResult(preorderDump)(statementsToString(compile(source), source))
  }
  // preorder traversal
  class PositionsCheckVisitor(source: String) extends core.AstVisitor {
    val buf = new StringBuilder()
    override def visitCommandBlock(node: core.CommandBlock): Unit = { visit(node); super.visitCommandBlock(node) }
    override def visitReporterApp(node: core.ReporterApp): Unit = { visit(node); super.visitReporterApp(node) }
    override def visitReporterBlock(node: core.ReporterBlock): Unit = { visit(node); super.visitReporterBlock(node) }
    override def visitStatement(node: core.Statement): Unit = { visit(node); super.visitStatement(node) }
    override def visitStatements(node: core.Statements): Unit = {
      if (node.stmts.isEmpty)
        buf.append(node.getClass.getSimpleName + " '' ")
      else visit(node)
      super.visitStatements(node)
    }
    def visit(node: core.AstNode): Unit = {
      val start = node.start - PREAMBLE.length
      val end = node.end - PREAMBLE.length
      val text =
        try "'" + source.substring(start, end) + "'"
        catch { case _: StringIndexOutOfBoundsException =>
          "out of bounds: " + ((start, end)) }
      buf.append(node.getClass.getSimpleName + " " + text + " ")
    }
  }
}
