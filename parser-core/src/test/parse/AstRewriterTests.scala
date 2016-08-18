// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite

import org.nlogo.core.{ CompilationOperand, CompilerException, DummyCompilationEnvironment,
  DummyExtensionManager, Femto, NetLogoCore, Program, TokenizerInterface }

class AstRewriterTests extends FunSuite {
  // these are basic checks that various AST structures can be rewritten when unaltered
  test("preserves source") {
    assertPreservesSource("""__ignore ifelse-value true [""] [ [] ]""")
    assertPreservesSource("fd 1")
    assertPreservesSource("let baz []")
    assertPreservesSource("create-turtles 10 [ fd 1 ]")
    assertPreservesSource("create-turtles 10 [ ]")
    assertPreservesSource("create-turtles 10")
    assertPreservesSource("foreach [1 2 3] print")
    assertPreservesSource("to foo end\n\nto baz end", "", "")
    assertPreservesSource("show reduce + [1 2 3]")
    assertPreservesSource("show reduce [?1 + ?2] [1 2 3]")
    assertPreservesSource("show [pycor] of one-of patches")
    assertPreservesSource("; comment with [\"a list\"] [1 2 3]\n")
    assertPreservesSource("__ignore (list (1 + 1) (2 - 1))")
    assertPreservesSource("__ignore [[x] -> list (1 + 1) (2 - 1)]")
    assertPreservesSource("show [ 1 2 3 ]")
    assertPreservesSource("show [ [1] [ 2] [ 3 ] ]")
    assertPreservesSource("show [ blue green yellow ]")
    assertPreservesSource("show (list 1 2 3) ; => [1 2 3]\n")
    assertPreservesSource("show reduce [[x y] -> x + y] [1 2 3]")
  }

  test("preserves complex source") {
    assertPreservesSource("to foo end\n\nto baz end", "", "")
    assertPreservesSource("to foo end\nto baz end", "", "")
    assertPreservesSource("to-report foo [bar] end\n\nto baz [qux] end", "", "")
    assertPreservesSource("breed [as a] breed [bs b] globals [glob1] to foo if is-a? glob1 [ create-bs (100 - (count as)) ] end", "", "")
    assertModifiesSource("to foo  \nend", "to foo\nend")
  }

  test("multi-source compilation") {
    val singleFileOp = compilationOp("to foo bar end\nto baz tick end")
    val multiFileOp = singleFileOp.copy(sources = singleFileOp.sources + ("other" -> "to bar tick end"))
    val rw = new AstRewriter(tokenizer, multiFileOp)
    val rewrittenSource = rw.rewrite(NoopFolder, rw.preserveBody _)
    assertResult("to foo bar end\nto baz tick end")(rewrittenSource)
  }

  test("replace token") {
    assertResult("__hsb-old 1 2 3")(replaceReporterToken("hsb 1 2 3", "hsb" -> "__hsb-old"))
    assertResult("ifelse true = false [ __hsb-old 1 2 3 ] [ __hsb-old 4 5 6 ]")(
      replaceReporterToken("ifelse true = false [ __hsb-old 1 2 3 ] [ __hsb-old 4 5 6 ]",
        "hsb" -> "__hsb-old"))
    assertResult("is-an-agent? one-of turtles")(replaceReporterToken("is-agent? one-of turtles", "is-agent?" -> "is-an-agent?"))
  }

  test("remove command") {
    assertResult("")(remove("fd 1", "fd"))
    assertResult("bk 1")(remove("fd 1 bk 1", "fd"))
    assertResult("create-turtles 10 [ fd 1 ]")(remove("create-turtles 10 [ fd 1 ]", "bk"))
    assertResult("create-turtles 10 [ ]")(remove("create-turtles 10 [ fd 1 ]", "fd"))
    assertResult("ask turtles [ ask one-of other turtles [ ] ]")(remove("ask turtles [ ask one-of other turtles [ set color blue ] ]", "set"))
    assertResult("fd 1 end to bar")(remove("fd 1 end to bar bk 2", "bk"))
    assertResult("fd 1 end to bar")(remove("fd 1 bk 1 end to bar", "bk"))
    assertResult("run [ user-message (word \"abc\" \"123\") ]")(remove("run [ user-message (word \"abc\" \"123\") ]", "fd"))
  }

  test("adds new command based on existing command") {
    assertResult("fd 1 bk 1")(addCommand("bk 1", "bk" -> "fd 1"))
    assertResult("fd 2 bk 2")(addCommand("bk 2", "bk" -> "fd {0}"))
    assertResult("fd exp 10 bk exp 10")(addCommand("bk exp 10", "bk" -> "fd {0}"))
    assertResult("set foo exp 10 bk exp 10")(addCommand("bk exp 10", "bk" -> "set foo {0}"))
    assertResult("set foo 2 setxy 1 2")(addCommand("setxy 1 2", "setxy" -> "set foo {1}"))
    assertResult("fd exp 3 setxy 2 exp 3")(addCommand("setxy 2 exp 3", "setxy" -> "fd {1}"))
    assertResult("ask turtles [  fd 1 bk 1 ]")(addCommand("ask turtles [ bk 1 ]", "bk" -> "fd 1"))
    assertResult("if (1 = 2) [  fd 1 bk 1 ]")(addCommand("if (1 = 2) [ bk 1 ]", "bk" -> "fd 1"))
  }

  test("rename command and manipulate arguments") {
    assertResult("file-close-all")(replaceCommand("file-close", "file-close" -> "file-close-all"))
    assertResult("fd 1")(replaceCommand("bk 1", "bk" -> "fd 1"))
    assertResult("fd exp 3")(replaceCommand("bk exp 3", "bk" -> "fd {0}"))
    assertResult("fd exp 3")(replaceCommand("setxy 2 exp 3", "setxy" -> "fd {1}"))
    assertResult("(fd exp 3)")(replaceCommand("setxy 2 exp 3", "setxy" -> "(fd {1})"))
  }

  test("replace reporter") {
    assertResult("2")(replaceReporter("1", "1" -> "2"))
    assertResult("\"z\"")(replaceReporter("\"a\"", "\"a\"" -> "\"z\""))
    assertResult("true")(replaceReporter("false", "false" -> "true")) // reality only exists in the mind
    assertResult("pi")(replaceReporter("e", "e" -> "pi"))
    assertResult("distance one-of other turtles")(replaceReporter("distance one-of turtles", "turtles" -> "other turtles"))
    assertResult("2 + 2")(replaceReporter("1 + 2", "1" -> "2"))
    assertResult("2 + 2 + 2")(replaceReporter("1 + 2", "1" -> "2 + 2"))
    assertResult("2 + 2 + 2")(replaceReporter("2 + 1", "1" -> "2 + 2"))
    assertResult("netlogo-web?")(replaceReporter("netlogo-applet?", "netlogo-applet?" -> "netlogo-web?"))
    // This doesn't work on literal lists right now.
    // This test just documents that behavior.
    // Could be added, but I don't really see a need most of the time.
    assertResult("[4 2 3]")(replaceReporter("[4 2 3]", "4" -> "1"))
  }

  test("lambda-ize") {
    testLambda("let baz []", "let baz []")
    testLambda("__ignore map [[_1] -> [size] of _1] (list turtle 0)", "__ignore map [[size] of ?] (list turtle 0)")
    testLambda("__ignore [[_1] -> print _1]", "__ignore task [print ?]")
    testLambda("__ignore reduce + [1 2 3]", "__ignore reduce + [1 2 3]")
    testLambda("""foreach [1 2 3] [[_1] ->  crt _1 run "set glob1 glob1 + count turtles" ]""",
      """foreach [1 2 3] [ crt ? run "set glob1 glob1 + count turtles" ]""")
    testLambda("__ignore map [[_1] -> round _1] [1 2 3]", "__ignore map [round ?] [1 2 3]")
    testLambda("__ignore (map [[_1 _2] -> _1 + _2] [1 2 3] [4 5 6])", "__ignore (map [?1 + ?2] [1 2 3] [4 5 6])")
    testLambda("__ignore sort-by [[_1 _2] -> _1 < _2] [1 2 3]", "__ignore sort-by [?1 < ?2] [1 2 3]")
    testLambda("foreach [] [[_1] -> foreach _1 [[_?1] -> set xcor _?1]]", "foreach [] [foreach ? [set xcor ?]]")
    testLambda("foreach n-values 4 [[_1] ->  _1] []", "foreach n-values 4 [ ? ] []")
    testLambda("let x 0 foreach [1 2 3] [[_1] -> set x _1]", "let x 0 foreach [1 2 3] [set x ?]")
    testLambda("foreach sort-by [[_1 _2] -> [size] of _1 > [size] of _2] turtles [[_1] -> ask _1 []]",
      "foreach sort-by [[size] of ?1 > [size] of ?2] turtles [ask ? []]")
    testLambda("__ignore (map [[_1 _2] -> list (_1 + 1) (_2 - 1)] (list 2 1))",
      "__ignore (map [list (?1 + 1) (?2 - 1)] (list 2 1))")
    testLambda("let a-task [[] -> tick]", "let a-task task tick")
    testLambda("let a-task [[] -> tick]", "let a-task task [tick]")
    testLambda("let a-value 1 let a-task [[] -> a-value]", "let a-value 1 let a-task task [a-value]")
  }

  test("add extension") {
    assertResult("extensions [foo]")(addExtension("", "foo"))
    assertResult("extensions [foo]")(addExtension("extensions [foo]", "foo"))
    assertResult("extensions [foo]\nto bar end")(addExtension("to bar end", "foo"))
    assertResult("extensions [foo baz] to bar end")(addExtension("extensions [foo] to bar end", "baz"))
    assertResult("extensions []\nglobals [x] to bar end")(addExtension("globals [x] to bar end", ""))
  }

  test("add global") {
    assertResult("globals [foo]")(addGlobal("", "foo"))
    assertResult("globals [foo]\nto bar end")(addGlobal("to bar end", "foo"))
    assertResult("globals [foo baz] to bar end")(addGlobal("GLOBALS [foo] to bar end", "baz"))
    assertResult("globals [foo qux baz] to bar end")(addGlobal("GLOBALS [foo qux] to bar end", "baz"))
  }

  val tokenizer: TokenizerInterface =
    Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")

  def compilationOp(source: String): CompilationOperand =
    new CompilationOperand(Map("" -> source),
      new DummyExtensionManager() {
        override def importExtension(path: String, errors: org.nlogo.core.ErrorSource): Unit = { }
      },
      new DummyCompilationEnvironment(),
      Program.fromDialect(NetLogoCore),
      subprogram = false)

  def rewriter(source: String): AstRewriter =
    new AstRewriter(tokenizer, compilationOp(source))

  def addExtension(source: String, extension: String): String = {
    val added = rewriter(source).addExtension(extension)
    added.trim
  }

  def addGlobal(source: String, global: String): String = {
    val added = rewriter(source).addGlobal(global)
    added.trim
  }

  def trimmedRewriteCommand(source: String, f: AstRewriter => String, preamble: String = "TO FOO ", postamble: String = " END"): String = {
    val src = preamble + source + postamble
    val rewritten = f(rewriter(src))
    rewritten.stripPrefix(preamble.trim).stripSuffix(postamble.trim).trim
  }

  def addCommand(source: String, target: (String, String)): String =
    trimmedRewriteCommand(source, _.addCommand(target))

  def replaceReporterToken(source: String, target: (String, String)): String =
    trimmedRewriteCommand(source, _.replaceToken(target._1, target._2), "TO-REPORT FOO REPORT ")

  def replaceCommand(source: String, target: (String, String)): String =
    trimmedRewriteCommand(source, _.replaceCommand(target))

  def remove(source: String, removeCommand: String): String =
    trimmedRewriteCommand(source, _.remove(removeCommand))

  def replaceReporter(source: String, target: (String, String)): String =
    trimmedRewriteCommand(source, _.replaceReporter(target), "TO-REPORT FOO REPORT ")

  def assertPreservesSource(source: String, header: String = "TO FOO ", footer: String = " END"): Unit = {
    val rewrittenSource =
      trimmedRewriteCommand(source,
        r => r.rewrite(NoopFolder, r.preserveBody _), header, footer)
    assert(source.trim == rewrittenSource, s"""expected: "${source.trim}", got: "${rewrittenSource}"""")
  }

  def assertModifiesSource(source: String, expectedSource: String): Unit = {
    val rewrittenSource =
      trimmedRewriteCommand(source, r => r.rewrite(NoopFolder, r.preserveBody _), "", "")
    assert(expectedSource == rewrittenSource, s"""expected: "${expectedSource}", got: "$rewrittenSource"""")
  }

  def lambdaize(source: String) =
    try {
      val rw = rewriter(source)
      rw.runVisitor(new Lambdaizer())
    } catch {
      case ex: CompilerException => fail(ex.getMessage + " " + source.slice(ex.start, ex.end))
    }

  def testLambda(changedBody: String, body: String, preamble: String = "TO FOO ", postamble: String = " END"): Unit = {
    val lambdaized = lambdaize(preamble + body + postamble)
    assertResult(preamble + changedBody + postamble, s"""expected: "$changedBody", got: "$lambdaized"""")(lambdaized)
  }
}
