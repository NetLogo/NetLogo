// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.api.{ DummyExtensionManager, Program, Token, TokenType }
import org.nlogo.nvm.Procedure

class IdentifierParserTests extends FunSuite {

  def compile(source: String): Iterator[Token] = {
    val wrappedSource = "to __test " + source + "\nend"
    val interfaceGlobals = {
      import collection.JavaConverters._
      List("X").asJava
    }
    val program = new Program(interfaceGlobals, false)
    implicit val tokenizer = Compiler.Tokenizer2D
    val results = TestHelper.structureParse(tokenizer.tokenizeAllowingRemovedPrims(wrappedSource), program)
    assertResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    new IdentifierParser(program, java.util.Collections.emptyMap[String, Procedure], results.procedures)
      .process(results.tokens(procedure).iterator, procedure)
      .iterator.takeWhile(_.tyype != TokenType.EOF)
  }

  test("empty") {
    assertResult("")(compile("").mkString)
  }
  test("interface global") {
    assertResult("Token(X,REPORTER,_observervariable:0)")(
      compile("print x").drop(1).mkString)
  }
  test("let") {
    assertResult("Token(let,COMMAND,_let)" + "Token(Y,REPORTER,_letvariable(Y))" + "Token(5,CONSTANT,5.0)")(
      compile("let y 5").mkString)
  }

}
