package org.nlogo.prim

import org.nlogo.core.{ Pure, Syntax, Token }
import org.nlogo.nvm.{ Reporter, Context }

case class _constsymbol(value: Token) extends Reporter with Pure {

  override def toString: String = value.text
  override def report(context: Context): AnyRef = value
}
