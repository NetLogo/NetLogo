// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _set extends Command {
  override def syntax =
    SyntaxJ.commandSyntax(Array(Syntax.WildcardType,
                               Syntax.WildcardType))
  override def perform(context: Context) {
    // we get compiled out of existence
    throw new UnsupportedOperationException
  }
}
