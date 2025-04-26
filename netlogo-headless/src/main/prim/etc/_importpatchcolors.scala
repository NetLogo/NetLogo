// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.ImportPatchColors.importPatchColors
import org.nlogo.nvm.{ Command, Context}
import org.nlogo.nvm.RuntimePrimitiveException

class _importpatchcolors extends Command {

  switches = true

  override def perform(context: Context): Unit = {
    try importPatchColors(
      workspace.fileManager.getFile(
        workspace.fileManager.attachPrefix(
          argEvalString(context, 0))),
      world, true)
    catch {
      case ex: java.io.IOException =>
        throw new RuntimePrimitiveException(
          context, this, token.text + ": " + ex.getMessage)
    }
    context.ip = next
  }
}
