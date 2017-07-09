package org.nlogo.prim.etc

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _symbol extends Reporter with Pure {


  override def report(context: Context): AnyRef =
    argEvalSymbol(context, 0).text
}
