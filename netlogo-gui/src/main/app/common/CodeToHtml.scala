// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import org.nlogo.api.Version
import org.nlogo.core.{ Dialect, Femto }
import org.nlogo.nvm.CompilerInterface

object CodeToHtml {
  // for standalone use, for example on a web server
  def main(argv:Array[String]) {
    val input = io.Source.fromInputStream(System.in).mkString
    println(newInstance.convert(input))
  }

  def newInstance = {
    val dialect =
      if (Version.is3D)
        Femto.scalaSingleton[Dialect]("org.nlogo.api.NetLogoThreeDDialect")
      else
        Femto.scalaSingleton[Dialect]("org.nlogo.api.NetLogoLegacyDialect")
    val compiler = Femto.get[CompilerInterface]("org.nlogo.compile.Compiler", dialect)
    new CodeToHtml(compiler)
  }
}

class CodeToHtml(compiler: CompilerInterface) {
  def convert(source:String, wrapped: Boolean = true): String = {
    val code = compiler.utilities.colorizer.toHtml(source)
    if (wrapped) s"<pre>$code\n</pre>\n" else code
  }
}
