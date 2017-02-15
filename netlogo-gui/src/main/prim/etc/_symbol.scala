package org.nlogo.prim.etc

import org.nlogo.api.Argument
import org.nlogo.core.Syntax
import org.nlogo.nvm.{Reporter, Context}
import org.nlogo.core.Pure

class _symbol extends Reporter with Pure {


  override def report(context: Context): AnyRef =
    argEvalSymbol(context, 0).text
}
