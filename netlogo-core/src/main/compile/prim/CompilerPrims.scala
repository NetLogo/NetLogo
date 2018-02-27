// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.prim

import org.nlogo.core.{ Command, Syntax }

case class _enterscope() extends Command {
  def syntax = Syntax.commandSyntax()
}

case class _exitscope() extends Command {
  def syntax = Syntax.commandSyntax()
}
