// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentIterator;
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

public final strictfp class _createlinksto
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  private final String breedName;

  public _createlinksto() {
    breedName = null;
  }

  public _createlinksto(String breedName) {
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
    mustNotBeUndirected(breed, context);
    checkForBreedCompatibility(breed, context);
    if (breed == world.links()) {
      breed.setDirected(true);
    }
    AgentSet edgeset = new org.nlogo.agent.ArrayAgentSet(AgentKindJ.Link(), agentset.count(),
        false, world);
    Turtle src = (Turtle) context.agent;
    // We have to shuffle here in order for who number assignment
    // to be random! - ST 3/15/06
    for (AgentIterator iter = agentset.shufflerator(context.job.random); iter.hasNext();) {
      Turtle dest = (Turtle) iter.next();
      if (world.linkManager.findLinkFrom(src, dest, breed, false) == null) {
        if (src == dest) {
          throw new EngineException
              (context, this,
                  I18N.errorsJ().get("org.nlogo.prim.$common.turtleCantLinkToSelf"));
        }
        if (src.id != -1 && dest.id != -1) {
          Link link = world.linkManager.createLink(src, dest, breed);
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
