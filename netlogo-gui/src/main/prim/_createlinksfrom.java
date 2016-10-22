// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.*;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _createlinksfrom
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  private final String breedName;

  public _createlinksfrom() {
    breedName = null;
    this.switches = true;
  }

  public _createlinksfrom(String breedName) {
    this.breedName = breedName;
    this.switches = true;
  }



  @Override
  public String toString() {
    return super.toString() + ":" + breedName + ",+" + offset;
  }

  @Override
  public void perform(final Context context) throws LogoException {
    AgentSet agentset = argEvalAgentSet(context, 0);
    if (agentset.kind() != AgentKindJ.Turtle()) {
      throw new ArgumentTypeException(
        context, this, 0, Syntax.TurtlesetType(), agentset);
    }
    AgentSet breed = breedName == null ? world.links() : world.getLinkBreed(breedName);
    mustNotBeUndirected(breed, context);
    checkForBreedCompatibility(breed, context);
    if (breed == world.links()) {
      breed.setDirected(true);
    }
    AgentSetBuilder edgeSetBuilder = new AgentSetBuilder(AgentKindJ.Link(), agentset.count());
    Turtle dest = (Turtle) context.agent;
    // We have to shuffle here in order for who number assignment
    // to be random! - ST 3/15/06
    for (AgentIterator iter = agentset.shufflerator(context.job.random); iter.hasNext();) {
      Turtle src = (Turtle) iter.next();
      if (world.linkManager.getLink(src, dest, breed).isEmpty()){
        if (src == dest) {
          throw new RuntimePrimitiveException
              (context, this,
                  I18N.errorsJ().get("org.nlogo.prim.$common.turtleCantLinkToSelf"));
        }
        if (src.id != -1 && dest.id != -1) {
          Link link = world.linkManager.createLink(src, dest, breed);
          edgeSetBuilder.add(link);
          workspace.joinForeverButtons(link);
        }
      }
    }
    IndexedAgentSet edgeSet = edgeSetBuilder.build();
    if (offset - context.ip > 2 && edgeSet.count() > 0) {
      context.runExclusiveJob(edgeSet, next);
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
