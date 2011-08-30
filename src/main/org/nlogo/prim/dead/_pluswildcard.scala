package org.nlogo.prim.dead

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

/**
 * This exists only to support parsing of old models by AutoConverter; in older NetLogos we allowed
 * + on types other than numbers.
 */

class _pluswildcard extends Reporter {
  override def report(context: Context) =
    throw new IllegalStateException
  override def syntax = {
    val left = Syntax.WildcardType
    val right = Array(Syntax.WildcardType)
    val ret = Syntax.WildcardType
    Syntax.reporterSyntax(left, right, ret, org.nlogo.api.Syntax.NormalPrecedence - 3)
  }
}
