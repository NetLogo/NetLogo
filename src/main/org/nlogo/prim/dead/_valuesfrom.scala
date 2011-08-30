package org.nlogo.prim.dead

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

/**
 * This isn't in the language anymore, but in order to auto-translate it to OF, we need to have a
 * class for it so the tokenizer and parser can parse it.
 */

class _valuesfrom extends Reporter {
  override def report(context: Context) =
    throw new IllegalStateException
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.AgentsetType, Syntax.ReporterBlockType),
                          Syntax.ListType)
}
