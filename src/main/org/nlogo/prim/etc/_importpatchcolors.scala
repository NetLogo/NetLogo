// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoException, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }
import org.nlogo.agent.ImportPatchColors.importPatchColors

class _importpatchcolors extends Command {

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.StringType), "O---", true)

  override def perform(context: Context) {
    try importPatchColors(
      workspace.fileManager.getFile(
        workspace.fileManager.attachPrefix(
          argEvalString(context, 0))),
      world, true)
    catch {
      case ex: java.io.IOException =>
        throw new EngineException(
          context, this, token.name + ": " + ex.getMessage)
    }
    context.ip = next
  }
}
