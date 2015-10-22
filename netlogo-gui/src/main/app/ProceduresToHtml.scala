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
    pico.getComponent(classOf[ProceduresToHtml])
  }
}
class ProceduresToHtml extends ProceduresToHtmlInterface {
  def convert(source:String):String = {
    s"<pre>${org.nlogo.parse.Colorizer.toHtml(source)}\n</pre>\n"
  }
}
