// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _myinlinks
    extends Reporter {
  private final String breedName;

  public _myinlinks() {
    breedName = null;
  }

  public _myinlinks(String breedName) {
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
  public Object report(final Context context) throws LogoException {
    AgentSet breed = breedName == null ? world.links() : world.getLinkBreed(breedName);
    mustNotBeUndirected(breed, context);
    return world.linkManager.findLinksTo
        ((Turtle) context.agent, breed);
  }
}
