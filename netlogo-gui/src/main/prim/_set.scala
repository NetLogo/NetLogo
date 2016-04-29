// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context }

class _set extends Command {

  override def perform(context: Context) {
    // we get compiled out of existence
    throw new UnsupportedOperationException
  }
}
