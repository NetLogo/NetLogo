package org.nlogo.prim.dead ;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

/**
 * This isn't in the language anymore, but in order to auto-translate
 * it to OF, we need to have a class for it so the tokenizer and parser
 * can parse it.
 */

class _valuefrom extends Reporter {
  override def report(context: Context) =
    throw new IllegalStateException
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TYPE_AGENT, Syntax.TYPE_REPORTER_BLOCK),
                          Syntax.TYPE_WILDCARD)
}
