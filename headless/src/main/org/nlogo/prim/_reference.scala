// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Reference, Context }

class _reference(_reference: Reference) extends Reporter {
  reference = _reference
  override def syntax =
    Syntax.reporterSyntax(Syntax.ReferenceType)
  override def toString =
    super.toString + ":" + reference.kind + "," + reference.vn
  override def report(context: Context) =
    // we're always supposed to get compiled out of existence
    throw new UnsupportedOperationException
}
