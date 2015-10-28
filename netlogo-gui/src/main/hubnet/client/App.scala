// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.core.Femto
import org.nlogo.api.{ NetLogoThreeDDialect, NetLogoLegacyDialect, Version }
import org.nlogo.nvm.CompilerInterface
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.window.VMCheck

/**
 * Creates and runs a hubnet client from the command line.
 */
object App {
  def main(args: Array[String]) {
    AbstractWorkspace.isApp(true)
    AbstractWorkspace.isApplet(false)
    VMCheck.detectBadJVMs()
    val compiler = new org.nlogo.nvm.DefaultCompilerServices(
      Femto.get[CompilerInterface]("org.nlogo.compiler.Compiler", if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect))
    ClientApp.mainHelper(args, new EditorFactory(compiler), compiler)
  }
}
