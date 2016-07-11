package org.nlogo.prim.etc

import org.nlogo.api.Argument
import org.nlogo.core.{ Pure, Syntax }
import org.nlogo.nvm.{Reporter, Context}

class _symbolstring extends Reporter with Pure {

  override def report(context: Context): AnyRef =
    argEvalSymbol(context, 0).text
}
