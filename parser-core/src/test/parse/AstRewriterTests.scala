// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite

import org.nlogo.core.{ CompilationOperand, DummyCompilationEnvironment,
  DummyExtensionManager, Femto, NetLogoCore, Program, TokenizerInterface }

class AstRewriterTests extends FunSuite {
  val tokenizer: TokenizerInterface =
    Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")

  def rewriter(source: String): AstRewriter = {
    val op = new CompilationOperand(Map("" -> source),
      new DummyExtensionManager() {
        override def importExtension(path: String, errors: org.nlogo.core.ErrorSource): Unit = { }
      },
      new DummyCompilationEnvironment(),
      Program.fromDialect(NetLogoCore),
      subprogram = false)
    new AstRewriter(tokenizer, op)
  }

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
    assert(source == rewrittenSource)
  }

  def assertModifiesSource(source: String, expectedSource: String): Unit = {
    val rewrittenSource =
      trimmedRewriteCommand(source, r => r.rewrite(NoopFolder, r.preserveBody _), "", "")
    assert(expectedSource == rewrittenSource)
  }

  // these are basic checks that various AST structures can be rewritten when unaltered
  test("preserves source") {
    assertPreservesSource("fd 1")
    assertPreservesSource("create-turtles 10 [ fd 1 ]")
    assertPreservesSource("create-turtles 10 [ ]")
    assertPreservesSource("create-turtles 10")
    assertPreservesSource("foreach [1 2 3] print")
    assertPreservesSource("to foo end\n\nto baz end", "", "")
    assertPreservesSource("show reduce + [1 2 3]")
    assertPreservesSource("show reduce [?1 + ?2] [1 2 3]")
    assertModifiesSource("to foo  \nend", "to foo\nend")
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
}
