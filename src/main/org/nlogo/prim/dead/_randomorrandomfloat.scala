package org.nlogo.prim.dead

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

/**
 * This primitive never existed; we convert "random" (in very, very old models) or
 * "random-int-or-float" (in more recent models) to this to force the user to change it; we need to
 * have a class for it so the tokenizer and parser can parse it.
 */

class _randomorrandomfloat extends Reporter {
  override def report(context: Context) =
    throw new IllegalStateException
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType),
                          Syntax.NumberType)
}
