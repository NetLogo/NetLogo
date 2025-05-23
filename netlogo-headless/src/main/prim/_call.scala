// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Activation, Command, Context, CustomGenerated, Procedure }

// Note that _call is "CustomGenerated".  That means that the bytecode generator generates custom
// bytecode for _call, instead of using the perform() method below.  The body of the perform()
// method below needs to be maintained in tandem with CustomGenerator.generateCall (as well as
// _callreport.report and CustomGenerator.generateCallReport). - ST 5/18/10

class _call(val procedure: Procedure)
extends Command with CustomGenerated {

  override def returnType =
     Syntax.VoidType

  override def toString =
    super.toString + ":" + procedure.name

  override def perform(context: Context): Unit = {
    val newActivation = new Activation(procedure, context.activation, next)
    var i = 0
    val limit = procedure.args.size - procedure.localsCount
    while(i < limit) {
      newActivation.args(i) = args(i).report(context)
      i += 1
    }
    context.activation = newActivation
    context.ip = 0
  }

}
