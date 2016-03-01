// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.{ Token, LogoList }
import org.nlogo.core.TokenType._

object DummyLiteralParser extends ((Token, Iterator[Token]) => AnyRef) {
  def apply(t: Token, i: Iterator[Token]): AnyRef = {
    t.tpe match {
      case Literal => t.value
      case OpenBracket => LogoList.fromIterator(i.takeWhile(_.tpe != CloseBracket).map(_.value))
      case _ => throw new Exception("dummyLiteralParser doesn't handle this token!")
    }
  }
}
