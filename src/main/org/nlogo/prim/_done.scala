// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

// We insert this as the last command in top-level procedures that we don't want to return.  That
// way the tight inner loop that steps through the commands never needs to do a bounds check when
// accessing the array; it only needs check the finished flag (which we need to be checking anyway
// since there are other places it gets set to true).

// We also put this after the commands inside an _ask, to stop the asked agent from executing the
// commands after the _ask.

class _done extends Command {
  override def syntax = Syntax.commandSyntax
  override def perform(context: Context) {
    perform_1(context)
  }
  def perform_1(context: Context) {
    context.finished = true
  }
}
