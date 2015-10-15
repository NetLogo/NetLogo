// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context }

class _set extends Command {
  // we get compiled out of existence
  override def perform(context: Context) =
    throw new UnsupportedOperationException
}
