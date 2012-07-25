// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

import java.util.ArrayList;
import java.util.List;

public final strictfp class _downhill
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.ReferenceType()},
            "-T--", true);
  }

  @Override
  public String toString() {
    if (world != null && reference != null) {
      return super.toString() + ":" + world.patchesOwnNameAt(reference.vn());
    } else {
      return super.toString();
    }
  }

  @Override
  public void perform(final Context context) {
    perform_1(context);
  }

  public void perform_1(final Context context) {
    Turtle turtle = (Turtle) context.agent;
    turtle.moveToPatchCenter();
    Patch patch = turtle.getPatchHere();
    double winningValue = Double.MAX_VALUE;
    List<Patch> winners = new ArrayList<Patch>();
    for (AgentIterator it = patch.getNeighbors().iterator(); it.hasNext();) {
      Patch tester = (Patch) it.next();
      Object value = tester.getPatchVariable(reference.vn());
      if (!(value instanceof Double)) {
        continue;
      }
      double dvalue = ((Double) value).doubleValue();
      // need to be careful here to handle properly the case where
      // dvalue equals Double.MAX_VALUE - ST 10/11/04, 1/6/07
      if (dvalue <= winningValue) {
        if (dvalue < winningValue) {
          winningValue = dvalue;
          winners.clear();
        }
        winners.add(tester);
      }
    }
    if (!winners.isEmpty() &&
        (!(patch.getPatchVariable(reference.vn()) instanceof Double) ||
            winningValue < ((Double) patch.getPatchVariable(reference.vn())).doubleValue())) {
      Patch winner = winners.get(context.job.random.nextInt(winners.size()));
      turtle.face(winner, true);
      try {
        turtle.moveTo(winner);
      } catch (AgentException ex) {
        // should be impossible
        throw new IllegalStateException(ex);
      }
    }
    context.ip = next;
  }
}
