// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// just enough functionality to make the tests pass

class DummyParserServices extends ParserServices {
  private def unsupported = throw new UnsupportedOperationException
  def readFromString(s: String): AnyRef =
    try { s.toDouble: java.lang.Double }
    catch {
      case ex: NumberFormatException =>
        s match {
          case "true" => true: java.lang.Boolean
          case "false" => false: java.lang.Boolean
          case _ => throw new CompilerException(
            "not a constant recognized by DummyParserServices", 0, s.size, "")
        }
    }
  def readNumberFromString(source: String) = source
  def isReporter(s: String): Boolean = unsupported
}
