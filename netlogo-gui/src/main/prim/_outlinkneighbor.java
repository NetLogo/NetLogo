// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Reporter;

public final strictfp class _outlinkneighbor
    extends Reporter {
  private final String breedName;

  public _outlinkneighbor() {
    breedName = null;
  }

  public _outlinkneighbor(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Boolean report(final org.nlogo.nvm.Context context) throws LogoException {
    org.nlogo.agent.LinkManager linkManager = world.linkManager;
    Turtle parent = (Turtle) context.agent;
    Turtle target = argEvalTurtle(context, 0);
    AgentSet breed = breedName == null ? world.links() : world.getLinkBreed(breedName);
    if (breed.isUndirected())
      return Boolean.FALSE;
    else {
      Link link = linkManager.findLinkFrom(parent, target, breed, true);
      if (link == null || link.getBreed().isUndirected())
        return Boolean.FALSE;
      else
        return Boolean.TRUE;
    }
  }
}
