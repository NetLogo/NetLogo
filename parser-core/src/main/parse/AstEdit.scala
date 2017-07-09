// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstNode, Instruction }

import AstFormat.Operation

case class AstEdit(operations: Map[AstPath, Operation], wsMap: WhitespaceMap) {
  def addOperation(position: AstPath, op: Operation) =
    copy(operations = operations + (position -> op))
}

object AstFormat {
  type Operation = (Formatter, AstNode, AstPath, AstFormat) => AstFormat
}

case class AstFormat(
  text: String,
  operations: Map[AstPath, Operation],
  instructionToString: Instruction => String,
  wsMap: FormattingWhitespace
)
