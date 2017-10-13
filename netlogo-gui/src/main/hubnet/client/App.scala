// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.core.Femto
import org.nlogo.api.{ NetLogoThreeDDialect, NetLogoLegacyDialect, Version }
import org.nlogo.util.NullAppHandler
import org.nlogo.nvm.PresentationCompilerInterface
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
    VMCheck.detectBadJVMs()
    // NOTE: While generally we shouldn't rely on a system property to tell
    // us whether or not we're in 3D, it's fine to do it here because:
    // * We only call this once, right at boot time.
    // * We never switch 2D / 3D in HubNet
    val compiler =
      Femto.get[PresentationCompilerInterface](
        "org.nlogo.compile.Compiler",
        if (Version.is3DInternal) NetLogoThreeDDialect else NetLogoLegacyDialect)
    ClientApp.mainHelper(args, compiler)
  }
}
