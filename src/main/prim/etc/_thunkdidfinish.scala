// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _thunkdidfinish extends Command {
  override def syntax = SyntaxJ.commandSyntax
  override def perform(context: Context) {
    workspace.completedActivations(context.activation) = true
    context.ip = next
  }
}
