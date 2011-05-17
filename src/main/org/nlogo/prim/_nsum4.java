package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _nsum4 extends Reporter {
  public int vn;

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_REFERENCE},
            Syntax.TYPE_NUMBER, "-TP-");
  }

  @Override
  public String toString() {
    if (world == null) {
      return super.toString() + ":" + vn;
    }
    return super.toString() + ":" + world.patchesOwnNameAt(vn);
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context);
  }

  public double report_1(final Context context) throws LogoException {
    Patch patch;
    if (context.agent instanceof Turtle) {
      patch = ((Turtle) context.agent).getPatchHere();
    } else {
      patch = (Patch) context.agent;
    }
    double sum = 0;
    for (AgentSet.Iterator it = patch.getNeighbors4().iterator(); it.hasNext();) {
      Object value = ((Patch) it.next()).getPatchVariable(vn);
      if (!(value instanceof Double)) {
        throw new EngineException (context, this,
                I18N.errors().getNJava("org.nlogo.prim.$common.noSumOfListWithNonNumbers",
                        new String[] {Dump.logoObject(value).toString(), Syntax.typeName(value)}));      }

      sum += ((Double) value).doubleValue();
    }
    return validDouble(sum);
  }
}
