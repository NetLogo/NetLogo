// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Procedure;
import org.nlogo.nvm.Reporter;

// Note that _callreport is "CustomGenerated".  That means that the
// bytecode generator generates custom bytecode for _callreport,
// instead of using the report() method below.  The body of the
// report() method below needs to be maintained in tandem with
// CustomGenerator.generateCallReport (as well as _call.perform and
// CustomGenerator.generateCall). - ST 5/18/10

public final strictfp class _callreport
    extends Reporter
    implements org.nlogo.nvm.CustomGenerated {
  public final Procedure procedure;

  public _callreport(Procedure procedure) {
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
  public Object report(Context context) throws LogoException {
    Activation newActivation =
        new Activation(procedure, context.activation, context.ip);
    for (int i = 0; i < (procedure.args.size() - procedure.localsCount); i++) {
      newActivation.args()[i] = args[i].report(context);
    }
    Object result = context.callReporterProcedure(newActivation);
    if (result == null) {
      throw new EngineException
          (context, this, "the " + procedure.name + " procedure failed to report a result");
    }
    return result;
  }
}
