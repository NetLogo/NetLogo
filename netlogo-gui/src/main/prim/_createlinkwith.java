// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.AgentKindJ;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _createlinkwith
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  private final String breedName;

  public _createlinkwith() {
    breedName = null;
    this.switches = true;
  }

  public _createlinkwith(String breedName) {
    this.breedName = breedName;
    this.switches = true;
  }



  @Override
  public String toString() {
    return super.toString() + ":" + breedName + ",+" + offset;
  }

  @Override
  public void perform(final Context context) throws LogoException {
    Turtle dest = argEvalTurtle(context, 0);
    Turtle src = (Turtle) context.agent;
    AgentSet breed = breedName == null ? world.links() : world.getLinkBreed(breedName);
    mustNotBeDirected(breed, context);
    checkForBreedCompatibility(breed, context);
    if (breed == world.links()) {
      breed.setDirected(false);
    }
    if (world.linkManager.findLinkEitherWay(src, dest, breed, false) == null) {
      if (src == dest) {
        throw new RuntimePrimitiveException
            (context, this,
                I18N.errorsJ().get("org.nlogo.prim.$common.turtleCantLinkToSelf"));
      }
      if (src.id > dest.id) {
        Turtle tmp = src;
        src = dest;
        dest = tmp;
      }
      if (src.id != -1 && dest.id != -1) {
        Link link = world.linkManager.createLink(src, dest, breed);
        workspace.joinForeverButtons(link);

        if (offset - context.ip > 2) {
          // Needed to run the commands with runExclusive(). I was not
          // clear how to run those commands otherwise.  -CLB 03/16/06
          context.runExclusiveJob(AgentSet.fromAgent(link), next);
        }
      }
    }
    context.ip = offset;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.block();
    a.done();
    a.resume();
  }
}
