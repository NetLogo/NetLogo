// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.Token

import scala.collection.BufferedIterator

trait TokenConsolidator[A] {
  def initialState: A
  def transform(t: Token, state: A, hasNext: Boolean): (Seq[Token], A)
}

class ConsolidatingIterator[A](i: Iterator[Token], transformer: TokenConsolidator[A]) extends Iterator[Token] {
  private var state: A = transformer.initialState
  private var outputQueue: Seq[Token] = Seq()

  override def hasNext = i.hasNext || outputQueue.nonEmpty

  override def next(): Token = {
    if (outputQueue.nonEmpty) {
      val result = outputQueue.head
      outputQueue = outputQueue.tail
      result
    } else if (i.hasNext) {
      val result = transformer.transform(i.next(), state, i.hasNext) match {
        case (xs, s_) if outputQueue.nonEmpty => {
          state = s_
          val result = outputQueue.head
          outputQueue = outputQueue.tail ++ xs
          result
        }
        case (Seq(), s_) => {
          state = s_
          next()
        }
        case (Seq(x, xs*), s_) => {
          state = s_
          outputQueue = xs
          x
        }
      }
      result
    } else {
      throw new NoSuchElementException
    }
  }
}

object ConsolidatingTokenStream {
  def apply[A](i: Iterator[Token], transformer: TokenConsolidator[A]): BufferedIterator[Token] = {
    new ConsolidatingIterator[A](i, transformer).buffered
  }
}
