// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.core.Femto
import org.nlogo.api.{ DummyExtensionManager, NetLogoThreeDDialect, NetLogoLegacyDialect, Version }
import org.nlogo.util.NullAppHandler
import org.nlogo.nvm.{ DefaultCompilerServices, PresentationCompilerInterface }
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.window.VMCheck

/**
 * Creates and runs a hubnet client from the command line.
 */
object App {
  def main(args: Array[String]): Unit = {
    mainWithAppHandler(args, NullAppHandler)
  }

  def mainWithAppHandler(args: Array[String], handler: Object): Unit = {
    handler.getClass.getDeclaredMethod("init").invoke(handler)
    handler.getClass.getDeclaredMethod("ready", classOf[AnyRef]).invoke(handler, this)
    AbstractWorkspace.isApp(true)
    VMCheck.detectBadJVMs()
    val compiler = new DefaultCompilerServices(
      Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler", if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect))
    ClientApp.mainHelper(args, compiler, new DummyExtensionManager)
  }
}
