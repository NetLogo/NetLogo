// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.{ TokenizerInterface, VersionHistory }
import org.nlogo.core.CompilerException
import org.nlogo.nvm.Workspace
import org.nlogo.prim._constdouble

// AutoConverter1 handles easy conversions that don't require parsing.
// AutoConverter2 handles hard conversions that do.
// This class was automatically converted from Scala to Java using a program called jatran.  I did
// some hand cleaning up of the code, but not that much, so beware. - ST 12/10/08

class AutoConverter2(workspace:Workspace,ignoreErrors:Boolean)(implicit tokenizer:TokenizerInterface) {
  def convert(originalSource:String, subprogram:Boolean, reporter:Boolean, version:String):String = {
    var source = originalSource
    if(source.trim.length == 0) return source
    if(VersionHistory.olderThan32pre4(version) || VersionHistory.olderThan3DPreview4(version)) {
      try { source = runVisitor(source, subprogram, reporter) }
      catch {
        // It's kind of crummy to just ignore exceptions, but we don't have a parser that can recover
        // from errors, and making the parser do that doesn't seem worth the effort.  For 4.0 we had
        // code that would notify the user when conversion failed, but threading that information
        // everywhere got complicated, so I'm throwing all that code away for 4.1 since most users
        // already converted to 4.0 - ST 12/18/08
        case ex:CompilerException => if(!ignoreErrors) throw new IllegalStateException(ex)
      }
      // Now that a temporary Program object is in place, we need to make sure that some bookkeeping
      // takes place in the World object since some stuff that happens later during loading, but
      // before a full recompile happens, expects usable World and Program objects to be
      // available. It's ugly, but the prospect of really straightening all of that stuff out
      // completely gives me nightmares. - ST 7/7/06
      workspace.world.realloc()
    }
    source
  }
  // On the conversion of VALUE-FROM, VALUES-FROM, HISTOGRAM-FROM:
  //
  // These are especially hard, harder than all the other auto-
  // conversions we've done in the past, in that it requires actual parsing,
  // not just tokenization.  To convert e.g.:
  //   value-from foo [bar]  =>  [bar] of foo
  // It's not enough to tokenize the code, because we need to figure out which
  // pair of brackets corresponds to which VALUE-FROM, and this is a difficult
  // problem in general; consider for example:
  //   values-from turtles with [foo] [bar] => [bar] of turtles with [foo]
  // In order to pair values-from with bar instead of foo, we need
  // to know the syntax of WITH.  We need to parse. - ST 7/1/06
  //
  // We have a chicken and egg problem during model loading.  First
  // we load the procedures, then we load the widgets.  Both need to
  // be autoconverted.  But it's not straightforwardly possible to
  // compile them because they can refer to each other.  In the
  // ordinary loading code, the cycle is broken by delaying
  // compilation of the Code tab until LoadEndEvent time.  But
  // now that AutoConverter invokes the parser, we need a smarter
  // solution, because we can't parse the code in the widgets
  // without the information in the Program object (put there when
  // StructureParser runs on the contents of the Code tab).
  // So here's what we do.  At the time the procedures section
  // loads, convertValueAndValuesFrom invokes IdentifierParser in
  // "forgiving" mode, which means that any unknown identifier in
  // the procedures must refer to an interface global we haven't
  // seen yet.  This is enough to partially fill the Program object
  // with information, enough information to make it possible to
  // parse the code in the widgets.  Finally, at LoadEndEvent time,
  // that partial Program object is discarded and replaced with a
  // full one when CompileManager does a full recompile of
  // everything. - ST 7/7/06
  //
  // A further wrinkle is the when we were only tokenizing, we
  // couldn't recover from tokenization failures during
  // autoconversion, which technically was a bug, but in practice we
  // didn't consider it a problem because it's very rare for someone
  // to have a saved model that doesn't even tokenize, and equally
  // rare for us to change the rules of what tokenizes and what
  // doesn't.  But it's far more common for us to change in the
  // language in ways that cause code not to parse; for example for
  // NetLogo 4.0 we are eliminating random-int-or-float and making
  // the + operator no longer work on strings and lists, only
  // numbers. So it's pretty essential that auto-conversion be able
  // to recover even when a parsing failure occurs.
  private def runVisitor(source:String, subprogram:Boolean, reporter:Boolean):String = {
    var preamble:String = ""
    val postamble:String = "\nend"
    var wrappedSource:String = null
    if(subprogram) {
      preamble = if(reporter) "to-report __convertValueAndValuesFrom report " else "to __convertValueAndValuesFrom "
      wrappedSource = preamble + source + postamble
    }
    else wrappedSource = source
    // This code is adapted from the first half or so of the compile() method. - ST 6/29/06
    val results:StructureParser.Results =
      new StructureParser(tokenizer.tokenizeAllowingRemovedPrims(wrappedSource), None,
                          workspace.world.program, workspace.getProcedures,
                          workspace.getExtensionManager, workspace.getCompilationEnvironment).parse(subprogram)
    val identifierParser = new IdentifierParser(workspace.world.program, workspace.getProcedures, results.procedures)
    val replacements = new collection.mutable.ArrayBuffer[Replacement]
    import collection.JavaConverters._ // results.procedures.values is a java.util.Collection
    for(procedure <- results.procedures.values.asScala) {
      val tokens = identifierParser.process(results.tokens(procedure).iterator, procedure)
      // So far this has been the same as compile().  What's different is that we proceed no farther
      // than the ExpressionParser phase.  Once the code is parsed, a visitor traverses the parsed
      // procedure, finds locations where replacements need to be made, and records them in a list.
      val procdefs = new ExpressionParser(procedure).parse(tokens)
      for(stmts <- procdefs.map(_.statements))
        stmts.accept(new AutoConverterVisitor(replacements, wrappedSource))
    }
    // Now that we know all the replacements that need to be made, we actually perform them.
    val buf:java.lang.StringBuilder = new java.lang.StringBuilder(source)
    var offset:Int = -preamble.length
    replacements.foreach{replacement =>
      try { offset = replacement.replace(buf, offset) }
      catch { case ex:Replacement.FailedException => org.nlogo.util.Exceptions.ignore(ex) }
    }
    if(!subprogram) {
      // so we can later convert widget code referring to these procedure names - ST 12/9/08
      workspace.setProcedures(results.procedures)
    }
    buf.toString
  }
  /**
   * These are the auto-conversions that require parsing (not just tokenization).
   *
   * 1) Finds occurrences of _valuefrom and changes them to _of.  Actually instead of making the
   * changes directly, we add Replacement objects to a list; the replacements will all be made at
   * once later.
   * Example: values-from turtles [color]
   * becomes: [color] of turtles
   * through two replacements:
   *   new Replacement(..., "values-from", "[color] of")
   *   new Replacement(..., " [color]", "")

   * The reason we do it in two replacements instead of one is to avoid damage to whitespace and/or
   * comments that may be interspersed in the code we are altering.  (We remove at most one space
   * before the left bracket.)
   *
   * 2) Convert _histogramfrom to _histogram + _of.
   *
   * 3) Attempt to convert random-or-random-float to either
   * random or random-float if we can deduce which is correct.
   *
   */
  private class AutoConverterVisitor(replacements:collection.mutable.ArrayBuffer[Replacement],source:String) extends DefaultAstVisitor {
    override def visitReporterApp(app:ReporterApp) {
      val oldReporter = app.reporter
      oldReporter match {
        case _:org.nlogo.prim.dead._valuefrom | _:org.nlogo.prim.dead._valuesfrom =>
          val arg1 = app(1).asInstanceOf[ReporterBlock]
          replacements += new Replacement(app.reporter.token.start,
                                          app.reporter.token.end,
                                          app.reporter.token.text,
                                          source.substring(arg1.start,arg1.end) + " of")
          var start = arg1.start
          if(start > 0 && source.charAt(start - 1) == ' ') start -= 1
          replacements += new Replacement(start,arg1.end,
                                          source.substring(start,arg1.end),"")
        case _:org.nlogo.prim.dead._randomorrandomfloat =>
          var choice:String = null
          val arg = app(0).asInstanceOf[ReporterApp].reporter
          if(arg.isInstanceOf[_constdouble] && arg.asInstanceOf[_constdouble].report(null).isInstanceOf[java.lang.Double])
            choice = if(arg.token.text.indexOf('.') == -1) "random" else "random-float"
          else if(arg.token.text.equalsIgnoreCase("WORLD-WIDTH") ||
                  arg.token.text.equalsIgnoreCase("WORLD-HEIGHT") ||
                  arg.token.text.equalsIgnoreCase("MAX-PXCOR") ||
                  arg.token.text.equalsIgnoreCase("MAX-PYCOR") )
            choice = "random"
          if(choice != null)
            replacements += new Replacement(oldReporter.token.start,
                                            oldReporter.token.end,
                                            oldReporter.token.text,
                                            choice)
        case _ => // do nothing
      }
      super.visitReporterApp(app)
    }
    // histogram-from turtles [xcor] => histogram [xcor] of turtles
    // replacement #1: "histogram-from" -> "histogram [xcor] of"
    // replacement #2: " [xcor]" -> ""
    override def visitStatement(stmt:Statement) {
      val oldCommand = stmt.command
      if(oldCommand.isInstanceOf[org.nlogo.prim.dead._histogramfrom]) {
        val arg1 = stmt(1).asInstanceOf[ReporterBlock]
        var start = arg1.start
        replacements += new Replacement(oldCommand.token.start,
                                        oldCommand.token.end,
                                        oldCommand.token.text,
                                        "histogram " + source.substring(start,arg1.end) + " of")
        if(start > 0 && source.charAt(start - 1) == ' ') start -= 1
        replacements += new Replacement(start,arg1.end,
                                        source.substring(start,arg1.end),"")
      }
      super.visitStatement(stmt)
    }
  }
}
