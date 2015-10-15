// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.api.{ DummyExtensionManager}
import org.nlogo.core.Program
import org.nlogo.core.Token
import org.nlogo.core.TokenType
import org.nlogo.nvm.Procedure

class IdentifierParserTests extends FunSuite {

  def compile(source: String): Iterator[Token] = {
    import collection.JavaConverters._
    val wrappedSource = "to __test " + source + "\nend"
    val interfaceGlobals = {
      List("X").asJava
    }
    val program = Program.empty().copy(interfaceGlobals = interfaceGlobals.asScala)
    implicit val tokenizer = Compiler.Tokenizer2D
    val results = TestHelper.structureParse(tokenizer.tokenizeAllowingRemovedPrims(wrappedSource), program)
    assertResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    new IdentifierParser(program, java.util.Collections.emptyMap[String, Procedure], results.procedures)
      .process(results.tokens(procedure).iterator, procedure)
      .iterator.takeWhile(_.tpe != TokenType.Eof)
  }

  test("empty") {
    assertResult("")(compile("").mkString)
  }
  test("interface global") {
    assertResult("Token(X,Reporter,_observervariable:0)")(
      compile("print x").drop(1).mkString)
  }
  test("let") {
    assertResult("Token(let,Command,_let)" + "Token(Y,Reporter,_letvariable(Y))" + "Token(5,Literal,5.0)")(
      compile("let y 5").mkString)
  }

}
