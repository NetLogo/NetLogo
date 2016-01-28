package org.nlogo.prim.etc

import org.nlogo.api.{Syntax, Argument}
import org.nlogo.nvm.{Reporter, Pure, Context}

class _symbol extends Reporter with Pure {
  override def syntax: Syntax = Syntax.reporterSyntax(Array(Syntax.SymbolType), Syntax.StringType)
  override def report(context: Context): AnyRef = argEvalSymbol(context, 0).name
}
