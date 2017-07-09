// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Context, Reporter }

class _lambdavariable(val varName: String) extends Reporter {
  override def toString =
    super.toString + ":" + varName
  override def report(context: Context): Nothing =
    // TaskVisitor compiles us out of existence
    throw new IllegalStateException
}
