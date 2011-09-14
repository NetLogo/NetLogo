package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.agent.Patch;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;

public final strictfp class _patchrow
    extends Reporter {
  @Override
  public Object report(Context context) throws LogoException {
    AgentSet result =
        new ArrayAgentSet(Patch.class, world.worldWidth(),
            false, world);
    double yDouble = argEvalDoubleValue(context, 0);
    int y = (int) yDouble;
    if (y == yDouble && y >= world.minPycor() && y <= world.maxPycor()) {
      int xMax = world.maxPxcor();
      for (int x = world.minPxcor(); x <= xMax; x++) {
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
