package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _linkbreed
    extends Reporter {
  String breedName;

  public _linkbreed(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Object report(final Context context) {
    return world.getLinkBreed(breedName);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TYPE_LINKSET);
  }

  public AgentSet report_1(final Context context) {
    return world.getLinkBreed(breedName);
  }
}
