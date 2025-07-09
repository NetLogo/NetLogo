// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core
import org.nlogo.core.{CompilerException}
import org.nlogo.core.SourceLocation
import org.scalatest.funsuite.AnyFunSuite

// This is where ExpressionParser gets most of its testing.  (It's a lot easier to test it as part
// of the overall front end than it would be to test in strict isolation.)

class FrontEndTests extends AnyFunSuite with BaseParserTest {
  test("DoParseSimpleCommand") {
    testParse("__ignore round 0.5", "_ignore()[_round()[_const(0.5)[]]]")
  }
  test("DoParseCommandWithInfix") {
    testParse("__ignore 5 + 2", "_ignore()[_plus()[_const(5.0)[], _const(2.0)[]]]")
  }
  test("DoParseTwoCommands") {
    testParse("__ignore round 0.5 fd 5",
      "_ignore()[_round()[_const(0.5)[]]] " +
      "_fd()[_const(5.0)[]]")
  }
  test("DoParseBadCommand1") {
    runFailure("__ignore 1 2 3", "Expected command.", 11, 12)
  }
  test("DoParseBadCommand2") {
    runFailure("__ignore", "__IGNORE expected 1 input.", 0, 8)
  }
  test("DoParseReporterOnly") {
    runFailure("round 1.2", "Expected command.", 0, 5)
  }
  test("WrongArgumentType") {
    runFailure("__ignore count 0", "COUNT expected this input to be an agentset, but got a number instead", 15, 16)
  }
  test("WrongArgumentTypeAgentVariable") {
    runFailure("__ignore [not heading] of turtles", "NOT expected this input to be a TRUE/FALSE, but got a number instead", 14, 21)
  }
  test("tooManyCloseBrackets") {
    runFailure("ask turtles [ fd 1 ] ] ]", "Expected command.", 21, 22)
  }
  test("missingCloseBracket") {
    // You could argue that it ought to point to the second bracket and not the first, but this is
    // fine. - ST 1/22/09
    runFailure("crt 10 [ [", "No closing bracket for this open bracket.", 7, 8)
  }
  test("missing name after let") {
    // here the error is at TokenType.Eof - ST 9/29/14
    runFailure("let", "LET expected 2 inputs.", 0, 3)
  }
  test("dangling argument errors as expected command") {
    runFailure("let _x 2 show 2 _x", "Expected command.", 16, 18)
  }
  test("unknown reporter failure") {
    runFailure("crt foo", "Nothing named FOO has been defined.", 4, 7)
  }
  test("parseSymbolUnknownName") {
    testParse("report __symbol foo", "_report()[_symbolstring()[_symbol()[]]]", preamble = "to-report sym ")
  }
  test("parseSymbolKnownName1") {
    testParse("report __symbol turtles", "_report()[_symbolstring()[_symbol()[]]]", preamble = "to-report sym ")
  }
  test("parseSymbolKnownName2") {
    testParse("report __symbol turtle", "_report()[_symbolstring()[_symbol()[]]]", preamble = "to-report sym ")
  }
  test("errorsOnParseSymbolWithArgs") {
    runFailure("report __symbol turtle 0", "Expected command.", 23, 24, preamble = "to-report sym ")
  }
  test("error-message used outside of carefully") {
    runFailure("let foo error-message", "error-message cannot be used outside of CAREFULLY.", 8, 21)
  }
  test("lambda parse") {
    testParse("__ignore [[x] -> x + x]",
      "_ignore()[_reporterlambda(X)[_plus()[_lambdavariable(X)[], _lambdavariable(X)[]]]]")
  }
  test("unbracketed lambda parse") {
    testParse("__ignore [x -> x + x]",
      "_ignore()[_reporterlambda(X)[_plus()[_lambdavariable(X)[], _lambdavariable(X)[]]]]")
  }
  test("unbracketed nested lambda parse") {
    testParse("__ignore [x -> [y ->  x / y ]]",
      "_ignore()[_reporterlambda(X)[_reporterlambda(Y)[_div()[_lambdavariable(X)[], _lambdavariable(Y)[]]]]]")
  }
  test("unbracketed zero-argument lambda parse") {
    testParse("__ignore [ -> 4]",
      "_ignore()[_reporterlambda()[_const(4)[]]]")
  }
  test("nested lambda parse") {
    testParse("__ignore [[x] -> [[y] ->  x / y ]]",
      "_ignore()[_reporterlambda(X)[_reporterlambda(Y)[_div()[_lambdavariable(X)[], _lambdavariable(Y)[]]]]]")
  }
  test("lambda parse with map") {
    testParse("__ignore map [[x] -> x] [1 2 3]",
      "_ignore()[_map()[_reporterlambda(X)[_lambdavariable(X)[]], _const([1, 2, 3])[]]]")
  }
  test("lambda parse with foreach") {
    testParse("foreach [1 2 3] [[x] -> __ignore x]",
      "_foreach()[_const([1, 2, 3])[], _commandlambda(X)[[_ignore()[_lambdavariable(X)[]]]]]")
  }
  test("lambda argument name is a literal") {
    runFailure("__ignore [[2] -> 2]", "Expected a variable name here", 11, 12)
  }
  test("invalidLiteral") {
    runFailure("__ignore [", "No closing bracket for this open bracket.", 9, 10)
  }
  test("invalidLambda1") {
    runFailure("__ignore [->", "No closing bracket for this open bracket.", 9, 10)
  }
  test("invalidLambda2") {
    runFailure("__ignore [[->", "No closing bracket for this open bracket.", 9, 10)
  }
  test("invalidLambda3") {
    runFailure("__ignore [[]->", "No closing bracket for this open bracket.", 9, 10)
  }
  test("invalidLambda4") {
    runFailure("__ignore [ foo bar -> ]", "An anonymous procedures of two or more arguments must enclose its argument list in brackets", 11, 18)
  }
  test("DoParseMap") {
    testParse("__ignore map [[x] -> round x] [1.2 1.7 3.2]",
      "_ignore()[_map()[_reporterlambda(X)[_round()[_lambdavariable(X)[]]], _const([1.2, 1.7, 3.2])[]]]")
  }
  test("DoParseMapShortSyntax") {
    testParse("__ignore map round [1.2 1.7 3.2]",
      "_ignore()[_map()[_reporterlambda(_0)[_round()[_lambdavariable(_0)[]]], _const([1.2, 1.7, 3.2])[]]]")
  }
  test("DoParseForeach") {
    runFailure("foreach [1 2 3] [__ignore ?]", "Nothing named ? has been defined.", 26, 27)
  }
  test("DoParseForeachShortSyntax") {
    testParse("foreach [1 2 3] print",
      "_foreach()[_const([1.0, 2.0, 3.0])[], _commandlambda(_0)[[_print()[_lambdavariable(_0)[]]]]]")
  }
  test("DoParseForeachWithDone") {
    runFailure("foreach [1 2 3] __done", "FOREACH expected at least 2 inputs, a list and an anonymous command.", 0, 7)
  }
  test("DoParselet") {
    testParse("let x 5 __ignore x",
      "_let(Let(X),x)[_const(5.0)[]] _ignore()[_letvariable(Let(X))[]]")
  }
  test("DoParseParenthesizedCommand") {
    testParse("(__ignore 5)",
      "_ignore()[_const(5.0)[]]")
  }
  test("DoParseParenthesizedCommandAsFromEvaluator") {
    testParse("__observercode (__ignore 5) __done",
      "_observercode()[] " +
      "_ignore()[_const(5.0)[]] " +
      "_done()[]")
  }
  test("DoParseCarefully") {
    testParse("carefully [ error \"foo\" ] [ __ignore error-message ]",
      """_carefully()[[_error()[_const(foo)[]]], [_ignore()[_errormessage()[]]]]""")
  }
  test("ParseExpressionWithInfix") {
    testParse("__ignore 5 + 2",
      "_ignore()[_plus()[_const(5.0)[], _const(2.0)[]]]")
  }
  test("ParseExpressionWithInfix2") {
    testParse("__ignore 5 + 2 * 7",
      "_ignore()[_plus()[_const(5.0)[], _mult()[_const(2.0)[], _const(7.0)[]]]]")
  }
  test("ParseExpressionWithInfix3") {
    testParse("__ignore 5 + 2 * 7 - 2",
      "_ignore()[_minus()[_plus()[_const(5.0)[], _mult()[_const(2.0)[], _const(7.0)[]]], _const(2.0)[]]]")
  }
  test("ParseExpressionWithInfixAndPrefix") {
    testParse("__ignore round 5.2 + log 64 2 * log 64 2 - random 2",
      "_ignore()[_minus()[_plus()[_round()[_const(5.2)[]], _mult()[_log()[_const(64.0)[], _const(2.0)[]], _log()[_const(64.0)[], _const(2.0)[]]]], _random()[_const(2.0)[]]]]")
  }
  test("ParseConstantInteger") {
    testParse("__ignore 5",
      "_ignore()[_const(5.0)[]]")
  }
  test("ParseConstantList") {
    testParse("__ignore [5]",
      "_ignore()[_const([5.0])[]]")
  }
  test("ParseConstantListWithSublists") {
    testParse("__ignore [[1] [2]]",
      "_ignore()[_const([[1.0], [2.0]])[]]")
  }
  test("ParseConstantListInsideLambda1") {
    testParse("__ignore n-values 10 [[]]",
      "_ignore()[_nvalues()[_const(10.0)[], _reporterlambda()[_const([])[]]]]")
  }
  test("ParseConstantListInsideLambda2") {
    testParse("__ignore n-values 10 [[5]]",
      "_ignore()[_nvalues()[_const(10.0)[], _reporterlambda()[_const([5.0])[]]]]")
  }
  test("ParseDiffuse") {
    testParse("clear-all diffuse pcolor 1",
      "_clearall()[] _diffuse()[_patchvariable(2)[], _const(1.0)[]]")
  }

  // in SetBreed2, we are checking that since no singular form of `fish`
  // is provided and it defaults to `turtle`, that the primitive `turtle`
  // isn't mistaken for a singular form and parsed as `_breedsingular` - ST 4/12/14
  test("SetBreed1") {
    testParse("__ignore turtle 0",
      "_ignore()[_turtle()[_const(0.0)[]]]")
  }
  test("SetBreed2") {
    testParse("__ignore turtle 0",
      "_ignore()[_turtle()[_const(0.0)[]]]")
  }

  /// duplicate name tests
  def frontEndError(src: String, error: String) = {
    val e = intercept[CompilerException] {
        FrontEnd.findIncludes(src)
      }
      assertResult(error)(e.getMessage.takeWhile(_ != ','))
  }

  test("findIncludes lists all includes when there is a valid includes statement") {
    assertResult(Seq())(FrontEnd.findIncludes(""))
    assertResult(Seq("foo.nls"))(FrontEnd.findIncludes("__includes ;; comment\n;; com2\n [\"foo.nls\"]"))
    frontEndError("__includes \"foo.nls\"]", "Did not find expected open bracket for __includes declaration")
    assertResult(Seq())(FrontEnd.findIncludes("__includes [ ]"))
    assertResult(Seq("foo.nls"))(FrontEnd.findIncludes("__includes [\"foo.nls\"]"))
    assertResult(Seq("foo.nls"))(FrontEnd.findIncludes("__includes [\"foo.nls\"] to foo show \"bar\" end"))
    assertResult(Seq("foo.nls"))(FrontEnd.findIncludes("__includes [\"foo.nls\"] foo \"bar\" end"))
    assertResult(Seq("foo.nls", "bar"))(FrontEnd.findIncludes("__includes [\"foo.nls\" foo \"bar\" end"))
  }

  test("findProcedurePositions maps procedures to their critical syntax tokens") {
    import org.nlogo.core.{ Token, TokenType }
    val procedurePos = FrontEnd.findProcedurePositions("""to foo show "bar" end""", None).get("foo")
    assert(procedurePos.nonEmpty)
    assert(procedurePos.get.declarationKeyword == Token("to", TokenType.Keyword, "TO")(SourceLocation(0, 2, "")))
    assert(procedurePos.get.identifier         == Token("foo", TokenType.Ident, "FOO")(SourceLocation(3, 6, "")))
    assert(procedurePos.get.endKeyword         == Token("end", TokenType.Keyword, "END")(SourceLocation(18, 21, "")))

    val procedurePos2 = FrontEnd.findProcedurePositions("""to foo end to bar show "foo" end""", None).get("bar")
    assert(procedurePos2.nonEmpty)
  }

  test("findProcedurePositions maps procedures to critical syntax tokens in a way that is tolerant of errors") {
    import org.nlogo.core.{ Token, TokenType }
    val procedurePos = FrontEnd.findProcedurePositions("""to foo show "bar" to bar show "foo" end""", None).get("foo")
    assert(procedurePos.get.identifier == Token("foo", TokenType.Ident, "FOO")(SourceLocation(3, 6, "")))
    assert(procedurePos.get.endKeyword == Token("end", TokenType.Keyword, "END")(SourceLocation(36, 39, "")))
    val unclosedProcedure = FrontEnd.findProcedurePositions("""to foo show""", None).get("foo")
    assert(unclosedProcedure.nonEmpty)
    assert(unclosedProcedure.get.identifier.text == "foo")
    val barProcedure = FrontEnd.findProcedurePositions("""to bar show "foo" end""", None).get("bar")
    assert(barProcedure.nonEmpty)
    assert(barProcedure.get.identifier.text == "bar")
    val noNameProcedure = FrontEnd.findProcedurePositions("""to show "foo" end""", None)
    assert(noNameProcedure.isEmpty)
  }

  test("regular old multi-let") {
    testParse("let multilet-var-1 [1 2 3] let v1 (item 0 multilet-var-1) let v2 (item 1 multilet-var-1)",
      "_let(Let(MULTILET-VAR-1),multilet-var-1)[_const([1, 2, 3])[]] _let(Let(V1),v1)[_item()[_const(0)[], _letvariable(Let(MULTILET-VAR-1))[]]] _let(Let(V2),v2)[_item()[_const(1)[], _letvariable(Let(MULTILET-VAR-1))[]]]")
  }

  test("fancy new multi-let") {
    testParse("let [v1 v2] [1 2 3]",
      "_multilet(List(_let(Let(V1),v1), _let(Let(V2),v2)))[_const([1, 2, 3])[]] _let(Let(V1),v1)[_multiassignitem()[]] _let(Let(V2),v2)[_multiassignitem()[]]")
  }

  test("regular old multi-set") {
    testParse("let v1 0 let v2 0 let multiset-var-1 [1 2 3] set v1 (item 0 multiset-var-1) set v2 (item 1 multiset-var-1)",
      "_let(Let(V1),v1)[_const(0)[]] _let(Let(V2),v2)[_const(0)[]] _let(Let(MULTISET-VAR-1),multiset-var-1)[_const([1, 2, 3])[]] _set()[_letvariable(Let(V1))[], _item()[_const(0)[], _letvariable(Let(MULTISET-VAR-1))[]]] _set()[_letvariable(Let(V2))[], _item()[_const(1)[], _letvariable(Let(MULTISET-VAR-1))[]]]")
  }

  test("fancy new multi-set") {
    testParse("let v1 0 let v2 0 set [v1 v2] [1 2 3]",
      "_let(Let(V1),v1)[_const(0)[]] _let(Let(V2),v2)[_const(0)[]] _multiset(List(_set(), _set()))[_const([1, 2, 3])[]] _set()[_letvariable(Let(V1))[], _multiassignitem()[]] _set()[_letvariable(Let(V2))[], _multiassignitem()[]]")
  }

  test("find extensions empty source") {
    assert(FrontEnd.findExtensions("").sameElements(Seq()))
  }

  test("find extensions no extensions") {
    assert(FrontEnd.findExtensions("globals [ a b c ] to test print \"test\" end to-report whee report 5 end")
                   .sameElements(Seq()))
  }

  test("find extensions one extension") {
    assert(FrontEnd.findExtensions("extensions [ array ]").sameElements(Seq("ARRAY")))
  }

  test("find extensions one extension after globals") {
    assert(FrontEnd.findExtensions("globals [ a b c ] extensions [ array ]").sameElements(Seq("ARRAY")))
  }

  test("find extensions multiple extensions") {
    assert(FrontEnd.findExtensions("extensions [ array bitmap csv ]").sameElements(Seq("ARRAY", "BITMAP", "CSV")))
  }

  test("find extensions funky whitespace") {
    assert(FrontEnd.findExtensions("extensions\n\n\t[     array\n\n\t\t\t\t    bitmap csv \t\t\t \n]")
                   .sameElements(Seq("ARRAY", "BITMAP", "CSV")))
  }
}

object FrontEndTests {
  val extensionManager = new core.DummyExtensionManager() {
    override def anyExtensionsLoaded = true
    override def replaceIdentifier(name: String): core.Primitive =
      name match {
        case "FOO:BAR" => new core.PrimitiveCommand() {
          override def getSyntax: core.Syntax =
            core.Syntax.commandSyntax(right = List(core.Syntax.ListType))
        }
        case "FOO:BAZ" => new core.PrimitiveReporter() {
          override def getSyntax: core.Syntax =
            core.Syntax.reporterSyntax(right = List(), ret = core.Syntax.ListType)
        }
        case _ => null
      }
  }
}
