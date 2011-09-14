package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _breed extends Reporter {
  String breedName;

  public _breed(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TurtlesetType());
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public AgentSet report_1(Context context) {
    return world.getBreed(breedName);
  }
}
