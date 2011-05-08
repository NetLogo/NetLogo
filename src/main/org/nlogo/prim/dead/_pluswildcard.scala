package org.nlogo.prim.dead

import org.nlogo.nvm.{Context, Reporter, Syntax}

/**
 * This exists only to support parsing of old models by AutoConverter; in older NetLogos we allowed
 * + on types other than numbers.
 */

class _pluswildcard extends Reporter {
  override def report(context: Context) =
    throw new IllegalStateException
  override def syntax = {
    val left = Syntax.TYPE_WILDCARD
    val right = Array(Syntax.TYPE_WILDCARD)
    val ret = Syntax.TYPE_WILDCARD
    Syntax.reporterSyntax(left, right, ret, Syntax.NORMAL_PRECEDENCE - 3)
  }
}
