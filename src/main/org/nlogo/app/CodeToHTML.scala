// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Color

import org.nlogo.api.ParserServices
import org.nlogo.window.EditorColorizer

import scala.annotation.tailrec
import scala.collection.mutable

class CodeToHTML(parser: ParserServices) {

  def apply(source: String): String = {
    // getCharacterColors gives us a color for every character, but we want to wait until
    // the color changes before we start a new font tag. So we group the colors into
    // sublists of equal colors.
    val colorGroups = group(new EditorColorizer(parser).getCharacterColors(source).toList)
    // use a mutable StringBuilder and tail recursion so we don't blow the stack - ST 6/30/09
    val result = new StringBuilder("<pre>")
    @tailrec def loop(source: String, colorGroups: List[List[Color]]): Unit = if(!colorGroups.isEmpty) {
      val group = colorGroups.head
      result ++= encodeWithColor(source.take(group.size), group.head)
      loop(source.drop(group.size), colorGroups.tail)
    }
    loop(source, colorGroups)
    result ++= "\n</pre>\n"
    result.toString
  }

  private def encodeWithColor(s: Seq[Char], color: Color) = {
    s"""<font color="${toHex(color)}">${s.map(escape).mkString}</font>"""
  }
  private def toHex(color: Color) = f"#${color.getRed}%02x${color.getGreen}%02x${color.getBlue}%02x"
  private def escape(c: Char) = Map('&' -> "&amp;", '<' -> "&lt;", '>' -> "&gt;", '"' -> "&quot;").getOrElse(c, c)

  // like group in Haskell. e.g. group(List(1,1,2,3,3,3)) = List(List(1,1),List(2),List(3,3,3)).
  // imperative using ListBuffer so we don't blow the stack on long inputs. - ST 6/30/09
  def group[T](xs: List[T]): List[List[T]] = {
    val result = new mutable.ListBuffer[List[T]]
    var rest = xs
    while(!rest.isEmpty) {
      val (firstGroup, more) = rest span(_ == rest.head)
      result += firstGroup
      rest = more
    }
    result.toList
  }
}
