// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, EngineException, Reporter }

// In testing I sometimes use this as an example of an unrejiggered primitive, so don't rejigger it
// (unless you go find and change those test cases). - ST 2/6/09

class _boom extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.WildcardType)
  override def report(context: Context): AnyRef =
    throw new EngineException(context, this, "boom!")
}
