package org.nlogo.prim.etc

import org.nlogo.api.Argument
import org.nlogo.core.Syntax
import org.nlogo.nvm.{Reporter, Pure, Context}

class _symbol extends Reporter with Pure {
  override def syntax: Syntax =
    Syntax.reporterSyntax(right = List(Syntax.SymbolType), ret = Syntax.StringType)

  override def report(context: Context): AnyRef =
    argEvalSymbol(context, 0).text
}
