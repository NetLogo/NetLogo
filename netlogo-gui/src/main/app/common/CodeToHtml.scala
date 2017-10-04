// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Component

import org.nlogo.core.{ Dialect, Femto }
import org.nlogo.api.{ Version, FileIO }
import org.nlogo.nvm.{ CompilerInterface, ModelTracker }
import org.nlogo.swing.UserAction,
  UserAction.{ FileCategory, FileExportSubcategory, MenuAction }

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

  class Action(modelTracker: ModelTracker, compiler: CompilerInterface, parent: Component, getText: () => String) extends
  ExportAction("code", modelTracker.guessExportName("code.html"), parent, { exportPath =>
    FileIO.writeFile(exportPath,
      new CodeToHtml(compiler).convert(getText()))
  }) with MenuAction {
    category    = FileCategory
    subcategory = FileExportSubcategory
    rank        = 6
  }
}

class CodeToHtml(compiler: CompilerInterface) {
  def convert(source:String, wrapped: Boolean = true): String = {
    val code = compiler.utilities.colorizer.toHtml(source)
    if (wrapped) s"<pre>$code\n</pre>\n" else code
  }
}
