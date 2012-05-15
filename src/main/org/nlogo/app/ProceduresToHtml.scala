// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import java.awt.Color
import org.nlogo.window.EditorColorizer
import org.nlogo.api.CompilerServices
object ProceduresToHtml {
  // for standalone use, for example on a web server
  def main(argv:Array[String]) {
    val input = io.Source.fromInputStream(System.in).mkString
    println(newInstance.convert(input))
  }
  def newInstance = {
    val pico = new org.nlogo.util.Pico
    pico.addComponent(classOf[ProceduresToHtml])
    pico.addComponent(classOf[org.nlogo.nvm.DefaultCompilerServices])
    pico.addScalaObject("org.nlogo.compiler.Compiler")
    pico.getComponent(classOf[ProceduresToHtml])
  }
}
class ProceduresToHtml(compiler:CompilerServices) extends ProceduresToHtmlInterface {
  def convert(source:String):String = {
    // getCharacterColors gives us a color for every character, but we want to wait until
    // the color changes before we start a new font tag.  So we group the colors into
    // sublists of equal colors.
    val colorGroups = group(new EditorColorizer(compiler).getCharacterColors(source).toList)
    // use a mutable StringBuilder and tail recursion so we don't blow the stack - ST 6/30/09
    val result = new StringBuilder("<pre>")
    def loop(source:String,colorGroups:List[List[Color]]) {
      if(!colorGroups.isEmpty) {
        val group = colorGroups.head
        result ++= encode(source.take(group.size),group.head)
        loop(source.drop(group.size),colorGroups.tail)
      }
    }
    loop(source,colorGroups)
    result ++= "\n</pre>\n"
    result.toString
  }
  private def encode(source:Seq[Char],color:Color) = {
    val hexColor = toHex(color.getRed) + toHex(color.getGreen) + toHex(color.getBlue)
    "<font color=\"#" + hexColor + "\">" + source.map(escape).mkString + "</font>"
  }
  private def toHex(i:Int) = (if(i < 16) "0" else "") + i.toHexString
  private def escape(c:Char) = escapeMap.getOrElse(c,c)
  private val escapeMap = Map('&' -> "&amp;",
                              '<' -> "&lt;",
                              '>' -> "&gt;",
                              '"' -> "&quot;")
  // like group in Haskell. e.g. group(List(1,1,2,3,3,3)) = List(List(1,1),List(2),List(3,3,3)).
  // imperative using ListBuffer so we don't blow the stack on long inputs. - ST 6/30/09
  private def group[T](xs:List[T]):List[List[T]] = {
    val result = new collection.mutable.ListBuffer[List[T]]
    var rest = xs
    while(!rest.isEmpty) {
      val (firstGroup, more) = rest.span(_ == rest.head)
      result += firstGroup
      rest = more
    }
    result.toList
  }
}
