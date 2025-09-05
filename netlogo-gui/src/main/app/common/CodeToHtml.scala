// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Component

import org.nlogo.core.{ ColorizerTheme, Dialect, Femto }
import org.nlogo.api.{ Version, FileIO }
import org.nlogo.nvm.CompilerInterface
import org.nlogo.swing.UserAction,
  UserAction.{ FileCategory, FileExportSubcategory, MenuAction }
import org.nlogo.theme.{ ClassicTheme, DarkTheme, InterfaceColors, LightTheme }
import org.nlogo.workspace.AbstractWorkspace

import scala.io.Source

object CodeToHtml {
  // for standalone use, for example on a web server
  def main(argv:Array[String]): Unit = {
    val input = Source.fromInputStream(System.in).mkString
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

  class Action(workspace: AbstractWorkspace, parent: Component, getText: () => String) extends
  ExportAction("code", workspace.guessExportName("code.html"), parent, { exportPath =>
    FileIO.writeFile(exportPath,
      new CodeToHtml(workspace.compiler).convert(getText()))
  }) with MenuAction {
    category    = FileCategory
    subcategory = FileExportSubcategory
    rank        = 6
  }
}

class CodeToHtml(compiler: CompilerInterface) {
  def convert(source:String, wrapped: Boolean = true): String = {
    val theme: ColorizerTheme = InterfaceColors.getTheme match {
      case ClassicTheme => ColorizerTheme.Classic
      case LightTheme => ColorizerTheme.Light
      case DarkTheme => ColorizerTheme.Dark
    }
    val code = compiler.utilities.colorizer.toHtml(source, theme)
    if (wrapped) s"<pre>$code\n</pre>\n" else code
  }
}
