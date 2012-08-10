// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, Let }
import org.nlogo.nvm.{ Command, Context }

// This isn't rejiggered yet because of the extra, unevaluated argument. (I say "yet" because this
// shouldn't be that hard to work around.) - ST 2/6/09

class _let extends Command {
  var let: Let = null
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.WildcardType, Syntax.WildcardType))
  override def perform(context: Context) {
    context.let(let, args(1).report(context))
    context.ip = next
  }
}
