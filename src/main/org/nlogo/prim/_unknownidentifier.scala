// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

// This is used by IdentifierParser's "forgiving" mode, used by AutoConverter, in which unknown
// identifiers are assumed to be references to global variables that the compiler doesn't know
// about. - ST 7/7/06

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _unknownidentifier extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.WildcardType)
  override def report(context: Context) =
    throw new IllegalStateException
}
