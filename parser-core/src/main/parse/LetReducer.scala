// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.prim._let
import org.nlogo.core.{Statement, AstTransformer}


// Any time we have a _let, it's _let[_letname, <reporterexpression>].
// Once we're done parsing, the _letname has already given us the information we need, so we remove it.
// This allows us to build an immutable, rejiggerable _let in the compiler backend, in addition to
// getting rid of a superfluous item in the AST.
class LetReducer extends AstTransformer {
  override def visitStatement(stmt: Statement): Statement = {
    stmt.instruction match {
      case l: _let => super.visitStatement(stmt.copy(args = stmt.args.tail))
      case _ => super.visitStatement(stmt)
    }
  }
}
