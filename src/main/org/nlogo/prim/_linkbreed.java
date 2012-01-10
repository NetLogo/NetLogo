// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

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
    return Syntax.reporterSyntax(Syntax.LinksetType());
  }

  public AgentSet report_1(final Context context) {
    return world.getLinkBreed(breedName);
  }
}
