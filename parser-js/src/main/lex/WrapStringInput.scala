// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.io.StringReader

object WrapStringInput {
  def apply(source: String, filename: String): WrappedInput =
    WrappedInput(new StringReader(source), filename)
}
