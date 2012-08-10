// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.LinkManager;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _linkneighbor extends Reporter {
  private final String breedName;

  public _linkneighbor() {
    breedName = null;
  }

  public _linkneighbor(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.AgentType()},
            Syntax.BooleanType(), "-T--");
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalTurtle(context, 0));
  }

  public boolean report_1(Context context, Turtle target) throws LogoException {
    Turtle parent = (Turtle) context.agent;
    AgentSet breed =
        breedName == null
            ? world.links()
            : world.getLinkBreed(breedName);
    mustNotBeDirected(breed, context);
    LinkManager linkManager = world.linkManager;
    return linkManager.findLinkFrom(parent, target, breed, true) != null
        || linkManager.findLinkFrom(target, parent, breed, true) != null;
  }
}
