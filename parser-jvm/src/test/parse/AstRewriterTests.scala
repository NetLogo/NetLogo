// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite

import org.nlogo.core.{ CompilationOperand, CompilerException, DummyCompilationEnvironment,
  DummyExtensionManager, DummyLibraryManager, Femto, NetLogoCore, Program, TokenizerInterface }

class AstRewriterTests extends FunSuite {
  // these are basic checks that various AST structures can be rewritten when unaltered
  test("preserves source") {
    assertPreservesSource("""__ignore ifelse-value true [""] [ [] ]""")
    assertPreservesSource("fd 1")
    assertPreservesSource("let baz []")
    assertPreservesSource("show (0.5)")
    assertPreservesSource("show ln(0.5)")
    assertPreservesSource("create-turtles 10 [ fd 1 ]")
    assertPreservesSource("create-turtles 10 [ ]")
    assertPreservesSource("create-turtles 10")
    assertPreservesSource("foreach [1 2 3] print")
    assertPreservesSource("to foo end\n\nto baz end", "", "")
    assertPreservesSource("show reduce + [1 2 3]")
    assertPreservesSource("show [pycor] of one-of patches")
    assertPreservesSource("; comment with [\"a list\"] [1 2 3]\n")
    assertPreservesSource("__ignore (list (1 + 1) (2 - 1))")
    assertPreservesSource("__ignore [ [x] -> list (1 + 1) (2 - 1)]")
    assertPreservesSource("show [ 1 2 3 ]")
    assertPreservesSource("show [ [1] [ 2] [ 3 ] ]")
    assertPreservesSource("show [ blue green yellow ]")
    assertPreservesSource("show (list 1 2 3) ; => [1 2 3]\n")
    assertPreservesSource("show reduce [ [x y] -> x + y] [1 2 3]")
    assertPreservesSource("show __block [foo bar baz]")
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
    assertResult("; is-agent? checks for turtles")(
      replaceReporterToken("; is-agent? checks for turtles", "is-agent?" -> "is-an-agent?"))
    assertResult("is-an-agent? one-of turtles ; is-agent? checks for turtles")(
      replaceReporterToken("is-agent? one-of turtles ; is-agent? checks for turtles", "is-agent?" -> "is-an-agent?"))
  }

  test("remove command") {
    assertResult("")(remove("fd 1", "fd"))
    assertResult("bk 1")(remove("fd 1 bk 1", "fd"))
    assertResult("; bk 1")(remove("; bk 1\n", "bk"))
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
    assertResult("; bk 2")(addCommand("; bk 2\n", "bk" -> "fd {0}"))
    assertResult("fd exp 10 bk exp 10")(addCommand("bk exp 10", "bk" -> "fd {0}"))
    assertResult("set foo exp 10 bk exp 10")(addCommand("bk exp 10", "bk" -> "set foo {0}"))
    assertResult("set foo 2 setxy 1 2")(addCommand("setxy 1 2", "setxy" -> "set foo {1}"))
    assertResult("fd exp 3 setxy 2 exp 3")(addCommand("setxy 2 exp 3", "setxy" -> "fd {1}"))
    assertResult("ask turtles [  fd 1 bk 1 ]")(addCommand("ask turtles [ bk 1 ]", "bk" -> "fd 1"))
    assertResult("if (1 = 2) [  fd 1 bk 1 ]")(addCommand("if (1 = 2) [ bk 1 ]", "bk" -> "fd 1"))
  }

  test("rename command and manipulate arguments") {
    assertResult("bk 1  forward 1")(replaceCommand("bk 1  fd 1", "fd" -> "forward {0}"))
    assertResult("file-close-all")(replaceCommand("file-close", "file-close" -> "file-close-all"))
    assertResult("fd 1")(replaceCommand("bk 1", "bk" -> "fd 1"))
    assertResult("; bk 1")(replaceCommand("; bk 1\n", "bk" -> "fd 1"))
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
    assertResult("2 ; 1 + 2")(replaceReporter("2 ; 1 + 2\n", "1" -> "2"))
    assertResult("2 + 2")(replaceReporter("1 + 2", "1" -> "2"))
    assertResult("2 + 2 + 2")(replaceReporter("1 + 2", "1" -> "2 + 2"))
    assertResult("2 + 2 + 2")(replaceReporter("2 + 1", "1" -> "2 + 2"))
    assertResult("netlogo-web?")(replaceReporter("netlogo-applet?", "netlogo-applet?" -> "netlogo-web?"))
    // This doesn't work on literal lists right now.
    // This test just documents that behavior.
    // Could be added, but I don't really see a need most of the time.
    assertResult("[4 2 3]")(replaceReporter("[4 2 3]", "4" -> "1"))
  }

  testLambda("let baz []", "let baz []")
  testLambda("__ignore map [ ?1 -> [size] of ?1 ] (list turtle 0)", "__ignore map [[size] of ?] (list turtle 0)")
  testLambda("__ignore [ ?1 -> print ?1 ]", "__ignore task [print ?]")
  testLambda("__ignore reduce + [1 2 3]", "__ignore reduce + [1 2 3]")
  testLambda("""foreach [1 2 3] [ ?1 -> crt ?1 run "set glob1 glob1 + count turtles" ]""",
    """foreach [1 2 3] [ crt ? run "set glob1 glob1 + count turtles" ]""")
  testLambda("__ignore map [ ?1 -> round ?1 ] [1 2 3]", "__ignore map [round ?] [1 2 3]")
  testLambda("__ignore (map [ [?1 ?2] -> ?1 + ?2 ] [1 2 3] [4 5 6])", "__ignore (map [?1 + ?2] [1 2 3] [4 5 6])")
  testLambda("__ignore sort-by [ [?1 ?2] -> ?1 < ?2 ] [1 2 3]", "__ignore sort-by [?1 < ?2] [1 2 3]")
  testLambda("foreach [] [ ?1 -> foreach ?1 [ ??1 -> set xcor ??1 ] ]", "foreach [] [foreach ? [set xcor ?]]")
  testLambda("foreach n-values 4 [ ?1 -> ?1 ] []", "foreach n-values 4 [ ? ] []")
  testLambda("let x 0 foreach [1 2 3] [ ?1 -> set x ?1 ]", "let x 0 foreach [1 2 3] [set x ?]")
  testLambda("foreach sort-by [ [?1 ?2] -> [size] of ?1 > [size] of ?2 ] turtles [ ?1 -> ask ?1 [] ]",
    "foreach sort-by [[size] of ?1 > [size] of ?2] turtles [ask ? []]")
  testLambda("__ignore (map [ [?1 ?2] -> list (?1 + 1) (?2 - 1) ] (list 2 1))",
    "__ignore (map [list (?1 + 1) (?2 - 1)] (list 2 1))")
  testLambda("let a-task [ [] -> tick ]", "let a-task task tick")
  testLambda("let a-task [ [] -> tick ]", "let a-task task [tick]")
  testLambda("foreach [1 2 3] [ ?1 -> crt ?1 ]", "foreach [1 2 3] task crt")
  testLambda("show (map ([ [?1 ?2] -> ?1 + ?2 ]) [1 2 3] [1 2 3])", "show (map (task +) [1 2 3] [1 2 3])")
  testLambda("show is-list? [ [] -> tick ]", "show is-list? task [tick]")
  testLambda("let a-value 1 let a-task [ [] -> a-value ]", "let a-value 1 let a-task task [a-value]")
  testLambda("baz ([ [] ->  fd 1 ]) ([ [?1 ?2] -> bk ?2 ])", "baz (task [ fd 1 ]) (task [ bk ?2 ])", preamble = "TO baz [a b] END TO FOO ")
  testLambda("show reduce [ [x y] -> x + y] [1 2 3]", "show reduce [[x y] -> x + y] [1 2 3]")
  testLambda("foreach [1 2 3] [ [x] -> show x ]", "foreach [1 2 3] [ [x] -> show x ]")
  testLambda("foreach [1 2 3] [ x -> show x ]", "foreach [1 2 3] [ x -> show x ]")
  testLambda("foreach [1 2 3] [ -> show 4 ]", "foreach [1 2 3] [ -> show 4 ]")
  testLambda("__ignore runresult [2 < (3 + pi)]", "__ignore runresult [2 < (3 + pi)]")
  testLambda("show filter [ ?1 -> ?1 < 3 ] [1 3 2]", "show filter [? < 3] [1 3 2]")
  testLambda("""let i map [ -> [ [] -> show "abc" ] ] [1 2 3]""",
    """let i map [task [show "abc"]] [1 2 3]""")

  test("add extension") {
    assertResult("extensions [foo]")(addExtension("", "foo"))
    assertResult("extensions [foo]")(addExtension("extensions [foo]", "foo"))
    assertResult("extensions [foo]\nto bar end")(addExtension("to bar end", "foo"))
    assertResult("extensions [foo baz] to bar end")(addExtension("extensions [foo] to bar end", "baz"))
    assertResult("extensions []\nglobals [x] to bar end")(addExtension("globals [x] to bar end", ""))
  }

  test("remove extension") {
    assertResult("")(removeExtension("extensions [abc]", "abc"))
    assertResult("extensions [ abc ]")(removeExtension("extensions [ abc def ]", "def"))
    assertResult("extensions [ def ]")(removeExtension("extensions [ abc def ]", "abc"))
    assertResult("extensions [ abc ghi ]")(removeExtension("extensions [ abc def ghi ]", "def"))
    assertResult("")(removeExtension("extensions [\n  abc\n]", "abc"))
    assertResult("extensions [\n  abc\n]")(removeExtension("extensions [\n  abc\n  def\n]", "def"))
    assertResult("extensions [\n  def\n]")(removeExtension("extensions [\n  abc\n  def\n]", "abc"))
    assertResult("extensions [abc]")(removeExtension("extensions [def abc]", "def"))
    assertResult("to go end")(removeExtension("extensions [abc]\nto go end", "abc"))

    assertResult("extensions []")(removeExtension("extensions []", "abc"))
    assertResult("extensions [ def ]")(removeExtension("extensions [ def ]", "abc"))
    assertResult("to go end")(removeExtension("to go end", "abc"))
  }

  test("add global") {
    assertResult("globals [foo]")(addGlobal("", "foo"))
    assertResult("globals [foo]\nto bar end")(addGlobal("to bar end", "foo"))
    assertResult("globals [foo baz] to bar end")(addGlobal("GLOBALS [foo] to bar end", "baz"))
    assertResult("globals [foo qux baz] to bar end")(addGlobal("GLOBALS [foo qux] to bar end", "baz"))
  }

  test("adds reporter procedure") {
    assertResult("globals [foo]\n\nto-report foo\nreport 3\nend")(
      addReporterProcedure("globals [foo]", "foo", "report 3"))
    assertResult("globals [foo]\nto-report a report 5 end\n\nto-report foo\nreport 3\nend")(
      addReporterProcedure("globals [foo]\nto-report a report 5 end", "foo", "report 3"))
    assertResult("globals [foo]\n\nto-report foo [ a ]\nreport a\nend")(
      addReporterProcedure("globals [foo]", "foo", "report a", Seq("a")))
  }

  val tokenizer: TokenizerInterface =
    Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")

  def compilationOp(source: String): CompilationOperand =
    new CompilationOperand(Map("" -> source),
      new DummyExtensionManager() {
        override def importExtension(path: String, errors: org.nlogo.core.ErrorSource): Unit = { }
      },
      new DummyLibraryManager,
      new DummyCompilationEnvironment(),
      Program.fromDialect(NetLogoCore),
      subprogram = false)

  def rewriter(source: String): AstRewriter = rewriter(compilationOp(source))

  def rewriter(op: CompilationOperand): AstRewriter =
    new AstRewriter(tokenizer, op)

  def addExtension(source: String, extension: String): String = {
    val added = rewriter(source).addExtension(extension)
    added.trim
  }

  def removeExtension(source:String, extension: String): String =
    rewriter(source).removeExtension(extension).trim

  def addGlobal(source: String, global: String): String = {
    val added = rewriter(source).addGlobal(global)
    added.trim
  }

  def addReporterProcedure(source: String, name: String, body: String, args: Seq[String] = Seq()): String = {
    val added = rewriter(source).addReporterProcedure(name, args, body)
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

  def testLambda(changedBody: String, body: String, preamble: String = "TO FOO ", postamble: String = " END"): Unit = {
    // note this uses a different token mapper
    def lambdaize(source: String) =
      try {
        val co = compilationOp(source)
        val op = co.copy(containingProgram =
          co.containingProgram.copy(dialect = new LambdaConversionDialect(co.containingProgram.dialect)))
        val rw = rewriter(op)
        rw.runVisitor(new Lambdaizer())
      } catch {
        case ex: CompilerException =>
          ex.printStackTrace()
          fail(ex.getMessage + " " + source.slice(ex.start, ex.end))
      }

    test("lambda-izes " + body + " to " + changedBody) {
      val lambdaized = lambdaize(preamble + body + postamble)
      assertResult(preamble + changedBody + postamble, s"""expected: "$preamble$changedBody$postamble", got: "$lambdaized"""")(lambdaized)
      try {
        val co = compilationOp(lambdaized)
        val op = co.copy(containingProgram =
          co.containingProgram.copy(dialect = new LambdaConversionDialect(co.containingProgram.dialect)))
        FrontEnd.frontEnd(op)
      } catch {
        case ex: CompilerException =>
          fail(s"expected converted source: '$lambdaized' to compile, errored with: " + ex.getMessage)
      }
    }
  }
}
