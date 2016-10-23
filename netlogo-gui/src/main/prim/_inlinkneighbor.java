// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _inlinkneighbor
    extends Reporter {
  private final String breedName;

  public _inlinkneighbor() {
    breedName = null;
  }

  public _inlinkneighbor(String breedName) {
    this.breedName = breedName;
  }



  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Object report(final Context context) throws LogoException {
    Turtle target = argEvalTurtle(context, 0);
    AgentSet breed = breedName == null ? world.links() : world.getLinkBreed(breedName);
    return world.linkManager.linksTo(target, (Turtle) context.agent, breed).length > 0;
  }
}
