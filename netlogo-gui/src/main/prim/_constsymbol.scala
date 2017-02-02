package org.nlogo.prim

import org.nlogo.core.{ Syntax, Token }
import org.nlogo.nvm.{Reporter, Context}
import org.nlogo.core.Pure

case class _constsymbol(value: Token) extends Reporter with Pure {

  override def toString: String = value.text
  override def report(context: Context): AnyRef = value
}
