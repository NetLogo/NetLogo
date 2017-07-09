// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
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
  public int returnType() {
    return procedure.syntax().ret();
  }



  @Override
  public String toString() {
    return super.toString() + ":" + procedure.name();
  }

  @Override
  public Object report(Context context) throws LogoException {
    Object[] callArgs = new Object[procedure.size()];
    for (int i = 0; i < (procedure.args().size() - procedure.localsCount()); i++) {
      callArgs[i] = args[i].report(context);
    }
    Activation newActivation =
        new Activation(procedure, context.activation, callArgs, context.ip);
    Object result = context.callReporterProcedure(newActivation);
    if (result == null) {
      throw new RuntimePrimitiveException
          (context, this, "the " + procedure.name() + " procedure failed to report a result");
    }
    return result;
  }
}
