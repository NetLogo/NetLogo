// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.util.{ Femto, NullAppHandler }
import org.nlogo.nvm.CompilerInterface
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
    AbstractWorkspace.isApplet(false)
    VMCheck.detectBadJVMs()
    val compiler = new org.nlogo.nvm.DefaultCompilerServices(
      Femto.scalaSingleton(classOf[CompilerInterface],
                           "org.nlogo.compiler.Compiler"))
    ClientApp.mainHelper(args, new EditorFactory(compiler), compiler)
  }
}
