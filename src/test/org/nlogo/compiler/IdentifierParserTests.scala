// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.api.{ DummyExtensionManager, Program, Token, TokenType }
import org.nlogo.nvm.Procedure

class IdentifierParserTests extends FunSuite {

  def compile(source: String): Iterator[Token] = {
    val wrappedSource = "to __test " + source + "\nend"
    val program = Program.empty().copy(interfaceGlobals = Seq("X"))
    implicit val tokenizer = Compiler.Tokenizer2D
    val results = new StructureParser(
      tokenizer.tokenize(wrappedSource), None,
      program, java.util.Collections.emptyMap[String, Procedure],
      new DummyExtensionManager)
      .parse(false)
    expect(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    new IdentifierParser(program, java.util.Collections.emptyMap[String, Procedure],
      results.procedures, false)
      .process(results.tokens(procedure).iterator, procedure)
      .iterator.takeWhile(_.tyype != TokenType.EOF)
  }

  test("empty") {
    expect("")(compile("").mkString)
  }
  test("interface global") {
    expect("Token(X,REPORTER,_observervariable:0)")(
      compile("print x").drop(1).mkString)
  }
  test("let") {
    expect("Token(let,COMMAND,_let)" + "Token(Y,REPORTER,_letvariable(Y))" + "Token(5,CONSTANT,5.0)")(
      compile("let y 5").mkString)
  }

}
