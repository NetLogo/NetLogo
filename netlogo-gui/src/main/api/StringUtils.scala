// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// This object holds methods that are used specifically with NetLogo strings.

object StringUtils {
  def escapeString(s: String): String =
    s.flatMap{
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case '\\' => "\\\\"
      case '\"' => "\\\""
      case c => c.toString
    }
  def unEscapeString(s: String): String = {
    val (stuff, more) = s.span(_ != '\\')
    if(more.size < 2)
      s
    else
      stuff + unescape(more(1)) + unEscapeString(more.drop(2))
  }
  def unescape(c: Char): Char =
    c match {
      case 'n' => '\n'
      case 'r' => '\r'
      case 't' => '\t'
      case '\\' => '\\'
      case '"' => '"'
      case _ =>
        // kludge alert: Tokenizer expects exactly this exception, so it
        // can detect the error and recover when it's in "robust" mode - ST 9/22/10
        throw new IllegalArgumentException(
          "invalid escape sequence: \\" + c)
    }
}
