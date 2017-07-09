// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ prim, Let, SourceLocation, Token, TokenType },
  prim.{ _lambdavariable, _letvariable, _lambdaargs, _unknownidentifier },
  TokenType._

// see also org.nlogo.core.TokenDSL
object PrimDSL {
  private val testLocation = SourceLocation(0, 0, "test")
  def `->`: Token              = Token("->", Reporter, _lambdaargs())(testLocation)
  def unid(str: String): Token = Token(str, Reporter, _unknownidentifier())(testLocation)
  def lamvar(str: String): Token = Token(str, Reporter, _lambdavariable(str.toUpperCase))(testLocation)
  def letvar(str: String): Token = Token(str, Reporter, _letvariable(Let()))(testLocation)
}
