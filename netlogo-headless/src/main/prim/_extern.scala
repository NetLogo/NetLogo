// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.{ api, nvm }

class _extern(command: api.Command) extends nvm.Command with nvm.CustomAssembled {

  override def toString =
    super.toString + ":+" + offset

  override def perform(context: nvm.Context) {
    val arguments =
      Array.tabulate[api.Argument](args.length)(i => new nvm.Argument(context, args(i)))
    try command.perform(
      arguments, new nvm.ExtensionContext(workspace, workspace.modelTracker, context))
    catch {
      case ex: api.ExtensionException =>
        val le = new nvm.RuntimePrimitiveException(
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
