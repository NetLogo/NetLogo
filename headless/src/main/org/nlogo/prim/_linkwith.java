// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Reporter;

public final strictfp class _linkwith
    extends Reporter {
  private final String breedName;

  public _linkwith() {
    breedName = null;
  }

  public _linkwith(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.AgentType()};
    int ret = Syntax.LinkType();
    return Syntax.reporterSyntax(right, ret, "-T--");
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    org.nlogo.agent.LinkManager linkManager = world.linkManager;
    Turtle parent = (Turtle) context.agent;
    Turtle target = argEvalTurtle(context, 0);
    AgentSet breed = breedName == null ? world.links() : world.getLinkBreed(breedName);
    mustNotBeDirected(breed, context);
    Link link = linkManager.findLinkEitherWay(parent, target, breed, true);
    if (link == null) {
      return org.nlogo.api.Nobody$.MODULE$;
    }
    return link;
  }
}
