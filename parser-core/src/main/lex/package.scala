// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import org.nlogo.core.TokenType

package object lex {
  type LexPredicate = Char => LexStates
  type TokenGenerator = String => Option[(String, TokenType, AnyRef)]
}
