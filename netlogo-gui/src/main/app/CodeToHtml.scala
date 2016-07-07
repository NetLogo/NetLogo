// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import org.nlogo.api.{ CompilerServices, Version }
import java.awt.Color
import org.nlogo.window.EditorColorizer
import org.nlogo.nvm.CompilerInterface

object CodeToHtml {
  // for standalone use, for example on a web server
  def main(argv:Array[String]) {
    val input = io.Source.fromInputStream(System.in).mkString
    println(newInstance.convert(input))
  }
  def newInstance = {
    val pico = new org.nlogo.util.Pico
    pico.addComponent(classOf[CodeToHtml])
    pico.add("org.nlogo.compiler.Compiler")
    if (Version.is3D)
      pico.addScalaObject("org.nlogo.api.NetLogoThreeDDialect")
    else
      pico.addScalaObject("org.nlogo.api.NetLogoLegacyDialect")
    pico.getComponent(classOf[CodeToHtml])
  }
}

class CodeToHtml(compiler: CompilerInterface) extends CodeToHtmlInterface {
  def convert(source:String):String = {
    s"<pre>${compiler.compilerUtilities.colorizer.toHtml(source)}\n</pre>\n"
  }
}
