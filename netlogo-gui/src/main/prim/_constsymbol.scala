package org.nlogo.prim

import org.nlogo.core.{ Pure, Token }
import org.nlogo.nvm.{ Context, Reporter }

case class _constsymbol(value: Token) extends Reporter with Pure {

  override def toString: String = value.text
  override def report(context: Context): AnyRef = value
}
