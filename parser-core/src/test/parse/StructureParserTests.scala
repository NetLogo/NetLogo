// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ CompilationOperand, DummyCompilationEnvironment, DummyExtensionManager,
  DummyLibraryManager, CompilerException, Femto, StructureResults }

import org.nlogo._
import org.scalatest.funsuite.AnyFunSuite

class StructureParserTests extends AnyFunSuite {

  val tokenizer: core.TokenizerInterface =
    Femto.scalaSingleton[core.TokenizerInterface]("org.nlogo.lex.Tokenizer")

  def compile(source: String): StructureResults =
    new StructureParser(None, false).parse(
      tokenizer.tokenizeString(source).map(Namer0), None, core.StructureResults.empty, "")

  def expectError(source: String, error: String): Unit = {
    val e = intercept[CompilerException] {
      compile(source)
    }
    assertResult(error)(e.getMessage.takeWhile(_ != ','))
  }

  test("empty") {
    val results = compile("")
    assert(results.procedures.isEmpty)
    assert(results.procedureTokens.isEmpty)
    assertResult("globals []\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds \n" +
      "link-breeds \n")(results.program.dump)
  }

  test("globals") {
    val results = compile("globals [foo bar]")
    assertResult("globals [FOO BAR]")(
      results.program.dump.split("\n").head)
  }

  test("turtles-own") {
    val results = compile("turtles-own [foo bar]")
    assertResult("turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE FOO BAR]")(
      results.program.dump.split("\n").drop(2).head)
  }

  test("breeds") {
    val results = compile("breed [mice mouse] breed [frogs frog]")
    assertResult("breeds MICE = Breed(MICE, MOUSE, , false)" +
           "FROGS = Breed(FROGS, FROG, , false)")(
      results.program.dump.split("\n").drop(5).take(2).mkString)
  }

  test("mice-own") {
    val results = compile("breed [mice mouse] mice-own [fur teeth]")
    assertResult("breeds MICE = Breed(MICE, MOUSE, FUR TEETH, false)")(
      results.program.dump.split("\n").drop(5).head)
  }

  test("command procedure") {
    val results = compile("to go fd 1 end")
    assertResult(1)(results.procedures.size)
    val proc = results.procedures(("GO", None))
    assertResult(false)(proc.isReporter)
    assertResult("procedure GO:[]{OTPL}:\n")(proc.dump)
  }

  test("two command procedures") {
    val results = compile("globals [g] to foo print 5 end to bar print g end")
    assertResult("globals [G]")(
      results.program.dump.split("\n").head)
    assertResult(2)(results.procedures.size)
    assertResult("procedure FOO:[]{OTPL}:\n")(results.procedures(("FOO", None)).dump)
    assertResult("")(results.procedures(("FOO", None)).displayName)
    assertResult("procedure BAR:[]{OTPL}:\n")(results.procedures(("BAR", None)).dump)
  }

  test("command procedure with empty args") {
    val results = compile("to go [] fd 1 end")
    assertResult(1)(results.procedures.size)
    val proc = results.procedures(("GO", None))
    assertResult(false)(proc.isReporter)
    assertResult("procedure GO:[]{OTPL}:\n")(proc.dump)
  }

  test("command procedure with some args") {
    val results = compile("to go [a b c] fd 1 end")
    assertResult(1)(results.procedures.size)
    val proc = results.procedures(("GO", None))
    assertResult(false)(proc.isReporter)
    assertResult("procedure GO:[A B C]{OTPL}:\n")(proc.dump)
  }

  test("reporter procedure") {
    val results = compile("to-report foo report 0 end")
    assertResult(1)(results.procedures.size)
    val proc = results.procedures(("FOO", None))
    assertResult(true)(proc.isReporter)
    assertResult("reporter procedure FOO:[]{OTPL}:\n")(proc.dump)
  }

  test("export") {
    val results = compile("export [foo bar baz]")
    assertResult(0)(results.procedures.size)
    assertResult(0)(results.imports.size)
    assertResult(true)(results.`export`.isDefined)
    assertResult(3)(results.`export`.head.exportedNames.size)
  }

  test("import") {
    val results = compile("import foo")
    assertResult(0)(results.procedures.size)
    assertResult(1)(results.imports.size)
    assertResult("FOO")(results.imports.head.moduleName)
    assertResult(None)(results.imports.head.alias)
  }

  test("import with alias") {
    val results = compile("import foo as bar")
    assertResult(0)(results.procedures.size)
    assertResult(1)(results.imports.size)
    assertResult("FOO")(results.imports.head.moduleName)
    assertResult(Some("BAR"))(results.imports.head.alias)
  }

  test("includes") {
    val results = compile("__includes [\"foo.nls\"]")
    assertResult(0)(results.procedures.size)
    assertResult(1)(results.includes.size)
    assertResult("foo.nls")(results.includes.head.value)
  }
  /// allow breeds to share variables

  test("breeds may share variables") {
    val results = compile("undirected-link-breed [edges edge]\n" +
      "breed [nodes node]\n" +
      "breed [foos foo]\n" +
      "edges-own [lweight]\n" +
      "nodes-own [weight]\n" +
      "foos-own [weight]")
    val dump = results.program.dump
    assert(dump.containsSlice(
      "breeds NODES = Breed(NODES, NODE, WEIGHT, false)\n" +
      "FOOS = Breed(FOOS, FOO, WEIGHT, false)\n" +
      "link-breeds EDGES = Breed(EDGES, EDGE, LWEIGHT, false)"), dump)
  }

  test("declarations1") {
    val results = compile("extensions [foo] globals [g1 g2] turtles-own [t1 t2] patches-own [p1 p2]")
    assert(results.procedures.isEmpty)
    assertResult("globals [G1 G2]\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE T1 T2]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR P1 P2]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds \n" +
      "link-breeds \n")(results.program.dump)
    assertResult("FOO")(results.extensions.map(_.value).mkString)
  }

  test("declarations2") {
    val results = compile("breed [b1s b1] b1s-own [b11 b12] breed [b2s b2] b2s-own [b21 b22]")
    assert(results.procedures.isEmpty)
    assertResult("globals []\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds B1S = Breed(B1S, B1, B11 B12, false)\n" +
      "B2S = Breed(B2S, B2, B21 B22, false)\n" +
      "link-breeds \n")(results.program.dump)
  }

  test("missing procedure name") {  // ticket #1183
    expectError("to", "identifier expected")
    expectError("to-report", "identifier expected") }
  test("missing open bracket after globals") {
    expectError("globals schmobals", "opening bracket expected") }
  test("bad top level keyword") {
    expectError("schmobals", "keyword expected") }
  test("missing close bracket after globals") {
    expectError("globals [", "closing bracket expected") }
  test("missing close bracket in globals") {
    expectError("globals [g turtles-own [t]",
      "Keyword TURTLES-OWN cannot be used in this context.") }
  test("constant in globals") {
    expectError("globals [d e f]",
      "Variable name conflicts with a constant.") }
  test("constant in turtles-own") {
    expectError("turtles-own [d e f]",
      "Variable name conflicts with a constant.") }
  test("constant in patches-own") {
    expectError("patches-own [d e f]",
      "Variable name conflicts with a constant.") }
  test("constant in links-own") {
    expectError("links-own [d e f]",
      "Variable name conflicts with a constant.") }
  test("constant in breed-own") {
    expectError("breed [tests test] tests-own [d e f]",
      "Variable name conflicts with a constant.") }
  test("constant in procedure input") {
    expectError("to test [d e f] end",
      "Input name conflicts with a constant.") }
  test("missing breed singular") {
    expectError("breed [xs]",
      "Breed declarations must have plural and singular. BREED [XS] has only one name.") }
  test("attempt primitive as variable") {
    expectError("globals [turtle]",
      "There is already a primitive reporter called TURTLE") }
  test("redeclaration of globals") {
    expectError("globals [] globals []",
      "Redeclaration of GLOBALS") }
  test("redeclaration of turtles-own") {
    expectError("turtles-own [] turtles-own []",
      "Redeclaration of TURTLES-OWN") }
  test("redeclaration of breed-own") {
    expectError("breed [hunters hunter] hunters-own [fear] hunters-own [loathing]",
      "Redeclaration of HUNTERS-OWN") }
  test("breeds-own for nonexistent breed") {
    expectError("hunters-own [fear]", "There is no breed \"HUNTERS\"")
  }
  test("redeclaration of breed plural") {
    expectError("breed [as a] breed [as b]", "There is already a breed called AS")
  }
  test("redeclaration of breed-singular") {
    expectError("breed [as a] breed [bs a]", "There is already a singular breed name called A")
  }
  test("redeclaration of extensions") {
    expectError("extensions [foo] extensions [bar]",
      "Redeclaration of EXTENSIONS") }

  def arrowError = "-> can only be used to create anonymous procedures"
  test("misuse of arrow as procedure name") { expectError("to -> end", arrowError) }
  test("misuse of arrow as argument") { expectError("to x [->] end", arrowError) }
  test("misuse of arrow as agent variable") { expectError("turtles-own [->]", arrowError) }

  test("missing close bracket in last declaration") {
    expectError("turtles-own [",
      "closing bracket expected") }

  test("breed singular clash with global") { // ticket #446
    expectError("breed [frogs frog] globals [frog]",
      "There is already a singular breed name called FROG") }

  test("breed owns / link breed owns clashes") {
    expectError("undirected-link-breed [edges edge] breed [nodes node] edges-own [weight] nodes-own [weight]",
      "There is already a EDGES-OWN variable called WEIGHT") }

  test("breed owns / turtles own clashes") {
    expectError("turtles-own [ test ] breed [ stuffs stuff ] stuffs-own [ test ]",
      "There is already a turtle variable called TEST")
  }

  test("duplicate turtles own clashes") {
    expectError("turtles-own [ test test ]",
      "There is already a turtle variable called TEST")
  }

  // https://github.com/NetLogo/NetLogo/issues/414
  test("missing end 1") {
    expectError("to foo to bar",
      "Keyword TO cannot be used in this context.") }
  test("missing end 2") {
    expectError("to foo fd 1",
      "END expected") }
  test("missing end 3") {
    expectError("to foo",
      "END expected") }
  test("missing end 4") {
    expectError("to foo [",
      "closing bracket expected") }
  test("missing close bracket in formals 1") {
    expectError("to foo [ end",
      "Keyword END cannot be used in this context.") }
  test("missing close bracket in formals 2") {
    expectError("to foo [ to",
      "Keyword TO cannot be used in this context.") }
  test("declaration after procedure") {
    expectError("to foo end globals []",
      "Keyword GLOBALS cannot be used in this context.") }

  test("singular breed name matches plural") {
    expectError("breed [ cats cats ]", "A breed cannot have the same plural and singular name")
  }

  def compileAll(src: String, nlsSrc1: String, nlsSrc2: String = ""): StructureResults = {
    StructureParser.parseSources(
      tokenizer,
      CompilationOperand( Map("" -> src), new DummyExtensionManager, new DummyLibraryManager
                        , new DummyCompilationEnvironment, subprogram = false),
      (_, name) =>
        name match {
          case "foo.nls" => Some(("foo.nls", nlsSrc1))
          case "bar.nls" if nlsSrc2.nonEmpty => Some(("bar.nls", nlsSrc2))
          case _ => None
        })
  }

  def expectParseAllError(src: String, error: String, nlsSrc1: String = "", nlsSrc2: String = "") = {
    val e = intercept[CompilerException] {
      compileAll(src, nlsSrc1, nlsSrc2)
    }
    assertResult(error)(e.getMessage.takeWhile(_ != ','))
  }

  test("import nonexistent module") {
    expectParseAllError("""import :foobar""", "Could not find foobar.nls")
  }

  test("import syntax returns correct results") {
    val results = compileAll("""import :foo""", "")
    assert(results.imports.nonEmpty || results.includedSources.nonEmpty)
  }

  test("import syntax detects duplicate imports") {
    expectParseAllError("import foo import bar import foo as baz", "Attempted to import a module multiple times")
  }

  test("import syntax detects import loops") {
    expectParseAllError(
      "import :foo",
      "Module FOO has already been imported",
      "import :bar",
      "import :foo")
  }

  test("import syntax default alias") {
    val src = """import :foo"""
    val nlsSrc = """
      |to test
      |  show 12345
      |end
      """.stripMargin
    val results = compileAll(src, nlsSrc)
    if (!results.procedures.contains(("FOO:TEST", None))) {
      fail()
    }
  }

  test("import syntax custom alias") {
    val src = """import :foo as bar"""
    val nlsSrc = """
      |to test
      |  show 12345
      |end
      """.stripMargin
    val results = compileAll(src, nlsSrc)
    if (!results.procedures.contains(("BAR:TEST", None))) {
      fail()
    }
  }

  test("import module from another module") {
    val mainSrc = """
      |import :foo
      |
      |to hello
      |  foo:hello
      |end
      """.stripMargin
    val fooSrc = """
      |import :bar
      |
      |to hello
      |  bar:hello
      |end
      """.stripMargin
    val barSrc = """
      |to hello
      |  show 123
      |end
      """.stripMargin
    val results = compileAll(mainSrc, fooSrc, barSrc)
    val expected = Set(
      ("HELLO", None),
      ("HELLO", Some("FOO")),
      ("FOO:HELLO", None),
      ("HELLO", Some("BAR")),
      ("BAR:HELLO", Some("FOO"))
    )

    assert(results.procedures.keys.toSet === expected)
  }

  test("import syntax name conflict") {
    val src = """
      |import :foo as a
      |
      |to a:test
      |  show 1234
      |end
      """.stripMargin
    val nlsSrc = """
      |to test
      |  show 5678
      |end
      """.stripMargin

    expectParseAllError(src, "There is already an imported procedure called A:TEST", nlsSrc)
  }

  test("invalid included file") {
    expectParseAllError("""__includes [ "foobar.nlogox" ]""", "Included files must end with .nls")
  }

  test("nonexistent included file") {
    expectParseAllError("""__includes [ "foobar.nls" ]""", "Could not find foobar.nls")
  }

  test("included file returns correct results") {
    val results = compileAll("""__includes [ "foo.nls" ]""", "")
    assert(results.includes.nonEmpty || results.includedSources.nonEmpty)
  }

  test("included file merges globals and turtle vars") {
    val src = """__includes [ "foo.nls" ] globals [ a b c ] breed [ mice mouse ] turtles-own [ t1 t2 ] mice-own [ m1 m2 ]"""
    val nlsSrc = "globals [ d f g ] turtles-own [ t3 t4 ] mice-own [ m3 m4 ]"
    val results = compileAll(src, nlsSrc)
    val expected = """globals [A B C D F G]
      |interfaceGlobals []
      |turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE T1 T2 T3 T4]
      |patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]
      |links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]
      |breeds MICE = Breed(MICE, MOUSE, M1 M2 M3 M4, false)
      |link-breeds""".stripMargin
    assertResult(expected)(results.program.dump.trim)
  }

  test("included file detects duplicate breed declaration") {
    expectParseAllError("""__includes [ "foo.nls" ] breed [ stuffs stuff ]""",
      "There is already a breed called STUFFS",
      "breed [ stuffs stuff ]")
  }

  test("included file detects duplicate turtles own variables") {
    expectParseAllError("""__includes [ "foo.nls" ] turtles-own [ test ]""",
      "There is already a turtle variable called TEST",
      "turtles-own [ test ]")
  }

  test("included file detects turtles own / breeds own clashes") {
    expectParseAllError("""__includes [ "foo.nls" ] turtles-own [ test ]""",
      "There is already a turtle variable called TEST",
      "breed [ stuffs stuff ] stuffs-own [ test ]")
  }

  test("included file detects breeds own / turtles own clashes") {
    expectParseAllError("""__includes [ "foo.nls" ] breed [ stuffs stuff ] stuffs-own [ test ]""",
      "There is already a STUFFS-OWN variable called TEST",
      "turtles-own [ test ]")
  }

  test("included file detects duplicate breeds own variables") {
    expectParseAllError("""__includes [ "foo.nls" ] breed [ stuffs stuff ] stuffs-own [ test ]""",
      "There is already a STUFFS-OWN variable called TEST",
      "stuffs-own [ test ]")
  }

  test("included file detects duplicate links own variables") {
    expectParseAllError("""__includes [ "foo.nls" ] links-own [ test ]""",
      "There is already a link variable called TEST",
      "links-own [ test ]")
  }

  test("included file detects duplicate undirected link breeds own variables") {
    expectParseAllError("""__includes [ "foo.nls" ] undirected-link-breed [ stuffs stuff ] stuffs-own [ test ]""",
      "There is already a STUFFS-OWN variable called TEST",
      "stuffs-own [ test ]")
  }

  test("included file detects duplicate directed link breeds own variables") {
    expectParseAllError("""__includes [ "foo.nls" ] directed-link-breed [ stuffs stuff ] stuffs-own [ test ]""",
      "There is already a STUFFS-OWN variable called TEST",
      "stuffs-own [ test ]")
  }

  test("included file detects links own / undirected link breeds own clashes") {
    expectParseAllError("""__includes [ "foo.nls" ] links-own [ test ]""",
      "There is already a link variable called TEST",
      "undirected-link-breed [ stuffs stuff ] stuffs-own [ test ]")
  }

  test("included file detects links own / directed link breeds own clashes") {
    expectParseAllError("""__includes [ "foo.nls" ] links-own [ test ]""",
      "There is already a link variable called TEST",
      "directed-link-breed [ stuffs stuff ] stuffs-own [ test ]")
  }

  test("included file detects undirected link breed / links own clashes") {
    expectParseAllError("""__includes [ "foo.nls" ] undirected-link-breed [ stuffs stuff ] stuffs-own [ test ]""",
      "There is already a STUFFS-OWN variable called TEST",
      "stuffs-own [ test ]")
  }

  test("included file detects directed link breed / links own clashes") {
    expectParseAllError("""__includes [ "foo.nls" ] directed-link-breed [ stuffs stuff ] stuffs-own [ test ]""",
      "There is already a STUFFS-OWN variable called TEST",
      "stuffs-own [ test ]")
  }

  test("mutually referrent sources") {
    val sources = Map[String, String](
      ""    -> "to foo bar end",
      "baz" -> "to bar foo end"
    )
    val results =
      StructureParser.parseSources(
        tokenizer, CompilationOperand( sources, new DummyExtensionManager, new DummyLibraryManager
                                     , new DummyCompilationEnvironment, subprogram = false))
    assert(results.procedures.contains(("FOO", None)) && results.procedures.contains(("BAR", None)))
  }
}
