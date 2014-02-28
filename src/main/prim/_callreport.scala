// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, CustomGenerated, Context, Procedure,
                       Activation, EngineException }

// Note that _callreport is "CustomGenerated".  That means that the bytecode generator generates
// custom bytecode for _callreport, instead of using the report() method below.  The body of the
// report() method below needs to be maintained in tandem with CustomGenerator.generateCallReport
// (as well as _call.perform and CustomGenerator.generateCall). - ST 5/18/10

class _callreport(val procedure: Procedure) extends Reporter with CustomGenerated {

  override def syntax = procedure.syntax

  override def toString =
    super.toString + ":" + procedure.name

  override def report(context: Context): AnyRef = {
    val newActivation = new Activation(procedure, context.activation, context.ip)
    val limit = procedure.args.size - procedure.localsCount
    var i = 0
    while(i < limit) {
      newActivation.args(i) = args(i).report(context)
      i += 1
    }
    val result = context.callReporterProcedure(newActivation)
    if (result == null)
      throw new EngineException(
        context, this, "the " + procedure.name + " procedure failed to report a result")
    result
  }

}
