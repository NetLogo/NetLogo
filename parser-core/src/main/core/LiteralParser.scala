// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait LiteralParser {
  @throws(classOf[CompilerException])
  def readFromString(s: String): AnyRef
  def readNumberFromString(source: String): AnyRef
}

