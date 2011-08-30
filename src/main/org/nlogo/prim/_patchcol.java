package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;

public final strictfp class _patchcol
    extends Reporter {
  @Override
  public Object report(Context context) throws LogoException {
    AgentSet result =
        new ArrayAgentSet(Patch.class, world.worldHeight(),
            false, world);
    double xDouble = argEvalDoubleValue(context, 0);
    int x = (int) xDouble;
    if (x == xDouble && x >= world.minPxcor() && x <= world.maxPxcor()) {
      int yMax = world.maxPycor();
      for (int y = world.minPycor(); y <= yMax; y++) {
        result.add(world.fastGetPatchAt(x, y));
      }
    }
    return result;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType()};
    int ret = Syntax.PatchsetType();
    return Syntax.reporterSyntax(right, ret);
  }
}
