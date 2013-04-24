// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.util.Femto
import org.nlogo.nvm.ParserInterface
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
    val parser = new org.nlogo.nvm.DefaultParserServices(
      Femto.scalaSingleton(classOf[ParserInterface],
                           "org.nlogo.parse.Parser"))
    ClientApp.mainHelper(args, new EditorFactory(parser), parser)
  }
}
