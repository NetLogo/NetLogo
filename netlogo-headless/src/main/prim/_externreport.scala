// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.{ api, nvm }

class _externreport(reporter: api.Reporter) extends nvm.Reporter {

  override def report(context: nvm.Context): AnyRef = {
    val arguments = Array.tabulate[api.Argument](args.length)(i =>
      new nvm.Argument(context, args(i)))
    try reporter.report(
      arguments, new nvm.ExtensionContext(workspace, workspace.modelTracker, context))
    catch {
      case ex: api.ExtensionException =>
        val ee = new nvm.RuntimePrimitiveException(
          context, this, "Extension exception: " + ex.getMessage)
        // it might be better to use setCause(), for the long term... but then i think the handler
        // would have to be changed, too.
        ee.setStackTrace(ex.getStackTrace)
        throw ee
    }
  }

}
