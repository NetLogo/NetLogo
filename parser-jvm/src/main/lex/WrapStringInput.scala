// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.text.StringCharacterIterator

object WrapStringInput {
  def apply(s: String, filename: String): WrappedInput =
    new CharacterIteratorInput(new StringCharacterIterator(s), 0, filename)
}
