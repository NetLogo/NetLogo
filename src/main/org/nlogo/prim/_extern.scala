// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.{ api, nvm }
import api.Syntax

class _extern(command: api.Command) extends nvm.Command with nvm.CustomAssembled {

  override def syntax = {
    val s = command.getSyntax
    val acs = command.getAgentClassString.split(":")
    if (acs(0).size < 4)
      acs(0) = Syntax.convertOldStyleAgentClassString(acs(0))
    if (acs.length >= 2) {
      if (acs(1).size < 4)
        acs(1) = Syntax.convertOldStyleAgentClassString(acs(1))
      Syntax.commandSyntax(
        s.right, s.dfault, acs(0), acs(1), command.getSwitchesBoolean)
    }
    else
      Syntax.commandSyntax(
        s.right, s.dfault, acs(0), null, command.getSwitchesBoolean)
  }

  override def toString =
    super.toString + ":+" + offset

  override def perform(context: nvm.Context) {
    val arguments =
      Array.tabulate[api.Argument](args.length)(i => new nvm.Argument(context, args(i)))
    try command.perform(
      arguments, new nvm.ExtensionContext(workspace, context))
    catch {
      case ex: api.ExtensionException =>
        val le = new nvm.EngineException(
          context, this, "Extension exception: " + ex.getMessage)
      // it might be better to use setCause(), for the long term... but then i think the handler
      // would have to be changed, too.
      le.setStackTrace(ex.getStackTrace)
      throw le
    }
    context.ip = offset
  }

  override def assemble(a: nvm.AssemblerAssistant) {
    a.add(this)
    command match {
      case ca: nvm.CustomAssembled =>
        ca.assemble(a)
      case _ =>
    }
    a.resume()
  }

}
