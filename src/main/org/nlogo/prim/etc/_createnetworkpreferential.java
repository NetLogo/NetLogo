package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

/**
 * This primitive creates links between an agentset of turtles,
 * by using Barabasi's preferential attachment model.
 * <p/>
 * ~Forrest (3/9/2007)
 */

public final strictfp class _createnetworkpreferential extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]
            {Syntax.TYPE_TURTLESET, Syntax.TYPE_LINKSET,
                Syntax.TYPE_NUMBER},
            "O---", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    AgentSet nodeSet = argEvalAgentSet(context, 0, Turtle.class);
    AgentSet linkBreed = argEvalAgentSet(context, 1, Link.class);
    int mEdges = argEvalIntValue(context, 2);

    if (linkBreed.isDirected()) {
      throw new EngineException(context, "This command only supports undirected link breeds.");
    }
    if (mEdges < 1) {
      throw new EngineException(context, "The number of neighbors to link to in each step must be at least 1.");
    }

    int numNodes = nodeSet.count();

    if (numNodes < mEdges + 1) {
      throw new EngineException(context, "This agentset has only " + numNodes + " members, so it is impossible to attach any turtle to " + mEdges + " neighbors!");
    }

    Turtle[] nodes = new Turtle[numNodes];
    int[] degreeCounts = new int[numNodes];
    boolean[] usedAlready = new boolean[numNodes];

    int i = 0;
    for (AgentSet.Iterator iter = nodeSet.iterator(); iter.hasNext();) {
      Agent agt = iter.next();
      if (!(agt instanceof Turtle)) {
        throw new EngineException
            (context, "You can only create links between turtles!");
      }
      nodes[i] = (Turtle) agt;
      i++;
    }

    int totalDegCount = 0;
    for (i = 0; i < mEdges; i++) {
      if (world.linkManager.findLink(nodes[i], nodes[mEdges], linkBreed, false) == null) {
        world.linkManager.createLink(nodes[i], nodes[mEdges], linkBreed);
      }
      degreeCounts[i]++;
      degreeCounts[mEdges]++;
      totalDegCount += 2;
    }

    for (i = mEdges + 1; i < nodes.length; i++) {
      int tempTotal = totalDegCount;
      for (int k = 0; k < i; k++) {
        usedAlready[k] = false;
      }
      for (int j = 0; j < mEdges; j++) {
        int randVal = context.job.random.nextInt(tempTotal);
        for (int k = 0; k < i; k++) {
          if (usedAlready[k]) {
            continue;
          }
          randVal -= degreeCounts[k];
          if (randVal > 0) {
            continue;
          }
          if (world.linkManager.findLink(nodes[i], nodes[k], linkBreed, false) == null) {
            world.linkManager.createLink(nodes[i], nodes[k], linkBreed);
          }
          tempTotal -= degreeCounts[k];
          degreeCounts[k]++;
          degreeCounts[i]++;
          totalDegCount += 2;
          usedAlready[k] = true;
          break;
        }
      }
    }
    context.ip = next;
  }
}
