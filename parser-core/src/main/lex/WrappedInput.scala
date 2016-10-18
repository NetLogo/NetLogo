// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.io.{Reader => JReader, BufferedReader}

import org.nlogo.core.Token

trait WrappedInput {
  def hasNext: Boolean
  def offset: Int
  def filename: String
  def longestPrefix(f: LexPredicate): (String, WrappedInput)
  def assembleToken(p: LexPredicate, f: TokenGenerator): Option[(Token, WrappedInput)]
}

object WrappedInput {
  def apply(reader: JReader, filename: String): WrappedInput =
    reader match {
      case br: BufferedReader => new BufferedInputWrapper(br, 0, filename)
      case r => new BufferedInputWrapper(reader, 0, filename)
    }
}
