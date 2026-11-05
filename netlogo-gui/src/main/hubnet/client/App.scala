// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.core.Femto
import org.nlogo.api.{ DummyExtensionManager, NetLogoThreeDDialect, NetLogoLegacyDialect, Version }
import org.nlogo.util.{ AppHandler, Application }
import org.nlogo.nvm.{ DefaultCompilerServices, PresentationCompilerInterface }
import org.nlogo.workspace.AbstractWorkspace

/**
 * Creates and runs a hubnet client from the command line.
 */
object App extends Application {
  def main(args: Array[String]): Unit = {
    mainWithAppHandler(args, new AppHandler)
  }

  def mainWithAppHandler(args: Array[String], handler: AppHandler): Unit = {
    handler.ready(this)
    AbstractWorkspace.isApp(true)
    val compiler = new DefaultCompilerServices(
      Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler", if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect))
    ClientApp.mainHelper(args, compiler, new DummyExtensionManager)
  }

  override def handleOpenPath(path: String): Unit = {}
  override def handleShowPreferences(): Unit = {}
  override def handleShowAbout(): Unit = {}
  override def handleQuit(): Unit = {}
}
