// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ Token, TokenType }

type StateType = (Option[Token], Boolean)

object ScopedIdentifierConsolidator extends TokenConsolidator[StateType] {
  def initialState = (None, false)

  override def transform(token: Token, state: StateType, hasNext: Boolean): (Seq[Token], StateType) = {
    val result = (token, state) match {
      case (t @ Token(_, TokenType.Colon, _), (Some(pendingToken), _)) =>
        (Seq(), (Some(pendingToken), true))

      case (t @ Token(text, TokenType.Ident, value: String), (Some(pendingToken), true)) => {
        val newText = pendingToken.text + ":" + text
        val newValue = pendingToken.value.asInstanceOf[String] + ":" + value
        val newSourcelocation = pendingToken.sourceLocation.copy(end = t.sourceLocation.end)
        val newStateToken = pendingToken.copy(text = newText, value = newValue)(newSourcelocation)

        (Seq(), (Some(newStateToken), false))
      }

      case (t @ Token(_, TokenType.Ident, _), (Some(pendingToken), _)) =>
        (Seq(pendingToken), (Some(t), false))

      case (t @ Token(_, TokenType.Ident, _), (None, _)) =>
        (Seq(), (Some(t), false))

      case (t, (Some(pendingToken), _)) => (Seq(pendingToken, t), (None, false))
      case (t, _) => (Seq(t), (None, false))
    }

    // Return the pending token if hasNext is false, meaning we are at the end of the stream.
    result match {
      case (ts, (Some(pendingToken), wasColon)) if !hasNext => (ts :+ pendingToken, (None, wasColon))
      case x => x
    }
  }
}
