package org.nlogo.prim

import org.nlogo.nvm.{Reporter, Pure, Context}
import org.nlogo.api.{Syntax, Token}

case class _constsymbol(value: Token) extends Reporter with Pure {
  override def syntax: Syntax = Syntax.reporterSyntax(Syntax.SymbolType)
  override def toString: String = value.name
  override def report(context: Context): AnyRef = value
}
