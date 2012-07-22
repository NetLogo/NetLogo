// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _createlinkswith
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  private final String breedName;

  public _createlinkswith() {
    breedName = null;
  }

  public _createlinkswith(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TurtlesetType(),
            Syntax.CommandBlockType() | Syntax.OptionalType()},
            "-T--", "---L", true);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName + ",+" + offset;
  }

  @Override
  public void perform(final Context context) throws LogoException {
    AgentSet agentset = argEvalAgentSet(context, 0);
    AgentSet breed = breedName == null ? world.links() : world.getLinkBreed(breedName);
    mustNotBeDirected(breed, context);
    checkForBreedCompatibility(breed, context);
    if (breed == world.links()) {
      breed.setDirected(false);
    }
    AgentSet edgeset = new org.nlogo.agent.ArrayAgentSet(AgentKindJ.Link(), agentset.count(),
        false, world);
    Turtle src = (Turtle) context.agent;
    // We have to shuffle here in order for who number assignment
    // to be random! - ST 3/15/06
    for (AgentSet.Iterator iter = agentset.shufflerator(context.job.random); iter.hasNext();) {
      Turtle dest = (Turtle) iter.next();
      if (world.linkManager.findLinkEitherWay(src, dest, breed, false) == null) {
        if (src == dest) {
          throw new EngineException
              (context, this,
                  I18N.errorsJ().get("org.nlogo.prim.$common.turtleCantLinkToSelf"));
        }
        if (src.id != -1 && dest.id != -1) {
          Link link;
          if (src.id > dest.id) {
            link = world.linkManager.createLink(dest, src, breed);
          } else {
            link = world.linkManager.createLink(src, dest, breed);
          }
          edgeset.add(link);
          workspace.joinForeverButtons(link);
        }
      }
    }
    if (offset - context.ip > 2 && edgeset.count() > 0) {
      context.runExclusiveJob(edgeset, next);
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
