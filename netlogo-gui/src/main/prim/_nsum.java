// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.api.TypeNames;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _nsum extends Reporter {
  public int vn;

  public _nsum(int vn) {
    super();
    this.vn = vn;
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

  public double report_1(Context context) throws LogoException {
    Patch patch;
    if (context.agent instanceof Turtle) {
      patch = ((Turtle) context.agent).getPatchHere();
    } else {
      patch = (Patch) context.agent;
    }
    double sum = 0;
    for (AgentIterator it = patch.getNeighbors().iterator(); it.hasNext();) {
      Object value = ((Patch) it.next()).getPatchVariable(vn);
      if (!(value instanceof Double)) {
        throw new RuntimePrimitiveException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.$common.noSumOfListWithNonNumbers",
                Dump.logoObject(value).toString(), TypeNames.name(value)));
      }

      sum += ((Double) value).doubleValue();
    }
    return validDouble(sum, context);
  }
}
