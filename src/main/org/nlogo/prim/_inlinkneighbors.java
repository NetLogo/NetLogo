// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _inlinkneighbors
    extends Reporter {
  private final String breedName;

  public _inlinkneighbors() {
    breedName = null;
  }

  public _inlinkneighbors(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.AgentsetType(), "-T--");
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    AgentSet breed = breedName == null
        ? world.links()
        : world.getLinkBreed(breedName);
    mustNotBeUndirected(breed, context);
    return world.linkManager.findLinkedTo
        ((Turtle) context.agent, breed);
  }
}
