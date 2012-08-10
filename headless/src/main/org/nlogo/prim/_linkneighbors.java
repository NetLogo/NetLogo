// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _linkneighbors extends Reporter {
  private final String breedName;

  public _linkneighbors() {
    breedName = null;
  }

  public _linkneighbors(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.AgentsetType(), "-T--");
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context);
  }

  public AgentSet report_1(Context context) throws LogoException {
    AgentSet breed = breedName == null ? world.links() : world.getLinkBreed(breedName);
    mustNotBeDirected(breed, context);
    return world.linkManager.findLinkedWith((Turtle) context.agent, breed);
  }
}
