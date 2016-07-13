// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ Token, TokenType }

trait TokenTransformer[A] {
  def initialState: A
  def transform(t: Token, state: A): (Token, A)
}

class SimpleTransformIterator[A](i: Iterator[Token], transformer: TokenTransformer[A]) extends Iterator[Token] {
  private var state: A = transformer.initialState

  override def hasNext = i.hasNext

  override def next(): Token = {
    val t = i.next()
    val (newT, s) = transformer.transform(t, state)
    state = s
    newT
  }
}

object TransformableTokenStream {
  def apply[A](i: Iterator[Token], transformer: TokenTransformer[A]): BufferedIterator[Token] = {
    new SimpleTransformIterator[A](i, transformer).buffered
  }
}
