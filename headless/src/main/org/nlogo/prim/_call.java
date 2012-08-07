// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Procedure;

// Note that _call is "CustomGenerated".  That means that the bytecode
// generator generates custom bytecode for _call, instead of using the
// perform() method below.  The body of the perform() method below
// needs to be maintained in tandem with CustomGenerator.generateCall
// (as well as _callreport.report and
// CustomGenerator.generateCallReport). - ST 5/18/10

public final strictfp class _call
    extends Command
    implements org.nlogo.nvm.CustomGenerated {
  public final Procedure procedure;

  public _call(Procedure procedure) {
    this.procedure = procedure;
  }

  @Override
  public Syntax syntax() {
    return procedure.syntax();
  }

  @Override
  public String toString() {
    return super.toString() + ":" + procedure.name;
  }

  @Override
  public void perform(Context context) throws LogoException {
    Activation newActivation = new Activation(procedure, context.activation, next);
    for (int i = 0; i < (procedure.args.size() - procedure.localsCount); i++) {
      newActivation.args()[i] = args[i].report(context);
    }
    context.activation = newActivation;
    context.ip = 0;
  }
}
