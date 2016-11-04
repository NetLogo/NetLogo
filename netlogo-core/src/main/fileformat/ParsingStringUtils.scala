// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

trait ParsingStringUtils {
  def stripLines(st: String): String =
    st.flatMap{
      case '\n' => "\\n"
      case '\\' => "\\\\"
      case '\"' => "\\\""
      case c => c.toString
    }

  def restoreLines(s: String): String = {
    @scala.annotation.tailrec
    def loop(acc: Vector[Char], rest: String): Vector[Char] = {
      if (rest.size < 2)
        acc ++ rest
      else if (rest.head == '\\')
        rest.tail.head match {
          case 'n'  => loop(acc :+ '\n', rest.tail.tail)
          case '\\' => loop(acc :+ '\\', rest.tail.tail)
          case '"'  => loop(acc :+ '"', rest.tail.tail)
      case _    => sys.error("invalid escape sequence in \"" + s + "\"")
        }
        else loop(acc :+ rest.head, rest.tail)
    }
    loop(Vector(), s).mkString
  }
}

object ParsingStringUtils extends ParsingStringUtils
