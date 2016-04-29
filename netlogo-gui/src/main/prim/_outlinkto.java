// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Reporter;

public final strictfp class _outlinkto
    extends Reporter {
  private final String breedName;

  public _outlinkto() {
    breedName = null;
  }

  public _outlinkto(String breedName) {
    this.breedName = breedName;
  }



  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    org.nlogo.agent.LinkManager linkManager = world.linkManager;
    AgentSet breed = breedName == null ? world.links() : world.getLinkBreed(breedName);
    mustNotBeUndirected(breed, context);
    Turtle parent = (Turtle) context.agent;
    Turtle target = argEvalTurtle(context, 0);
    Link link = linkManager.findLinkFrom(parent, target, breed, true);
    if (link == null) {
      return org.nlogo.core.Nobody$.MODULE$;
    }
    return link;
  }
}
