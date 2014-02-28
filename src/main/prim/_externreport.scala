// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.{ api, nvm }
import api.Syntax

class _externreport(reporter: api.Reporter) extends nvm.Reporter {

  override def syntax = {
    val s = reporter.getSyntax
    val acs = reporter.getAgentClassString.split(":")
    if (acs(0).size < 4)
      acs(0) = Syntax.convertOldStyleAgentClassString(acs(0))
    if (acs.length >= 2) {
      if (acs(1).size < 4)
        acs(1) = Syntax.convertOldStyleAgentClassString(acs(1))
      s.copy(agentClassString = acs(0), blockAgentClassString = acs(1))
    }
    else
      s.copy(agentClassString = acs(0), blockAgentClassString = null)
  }

  override def report(context: nvm.Context): AnyRef = {
    val arguments = Array.tabulate[api.Argument](args.length)(i =>
      new nvm.Argument(context, args(i)))
    try reporter.report(
      arguments, new nvm.ExtensionContext(workspace, context))
    catch {
      case ex: api.ExtensionException =>
        val ee = new nvm.EngineException(
          context, this, "Extension exception: " + ex.getMessage)
        // it might be better to use setCause(), for the long term... but then i think the handler
        // would have to be changed, too.
        ee.setStackTrace(ex.getStackTrace)
        throw ee
    }
  }
}
