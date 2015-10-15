package org.nlogo.prim

import org.nlogo.core.{ Syntax, Token }
import org.nlogo.nvm.{Reporter, Pure, Context}

case class _constsymbol(value: Token) extends Reporter with Pure {
  override def syntax: Syntax =
    Syntax.reporterSyntax(ret = Syntax.SymbolType)
  override def toString: String = value.text
  override def report(context: Context): AnyRef = value
}
