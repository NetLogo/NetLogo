// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentException;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final strictfp class _atpoints
    extends Reporter {
  @Override
  public Object report(final Context context) {
    // part 1: get arguments, context.checked validity
    AgentSet sourceSet = argEvalAgentSet(context, 0);
    List<Agent> result = new ArrayList<Agent>();
    LogoList points = argEvalList(context, 1);
    for (Iterator<Object> it = points.iterator(); it.hasNext();) {
      if (!validateListEntry(it.next())) {
        throw new EngineException(context, this, I18N.errorsJ().getN(
            "org.nlogo.prim.etc._atpoints.invalidListOfPoints", Dump.logoObject(points)));
      }
    }

    // part 2: figure out which patches are at the given points
    Set<Patch> patches =
        getPatchesAtPoints(context, context.agent, points);

    // part 3: construct a new agentset and return it
    if (sourceSet.kind() == AgentKindJ.Patch()) {
      if (sourceSet != world.patches()) {   //sourceSet is not the entire set of patches
        for (Iterator<Patch> iter = patches.iterator(); iter.hasNext();) {
          Patch patch = iter.next();
          if (sourceSet.contains(patch)) {
            result.add(patch);
          }
        }
      } else {  //sourceSet is the entire set of patches
        result.addAll(patches);
      }
    } else if (sourceSet.kind() == AgentKindJ.Turtle()) {
      if (sourceSet != world.turtles()) {  //sourceSet is not the entire set of turtles
        if (world.isBreed(sourceSet)) {  //source set is a breed
          for (Iterator<Patch> iter = patches.iterator(); iter.hasNext();) {
            Patch otherPatch = iter.next();
            for (Turtle tempTurtle : otherPatch.turtlesHere()) {
              if (sourceSet == tempTurtle.getBreed()) {
                result.add(tempTurtle);
              }
            }
          }
        } else {  //sourceSet not the entire set of turtles and is not a breed
          for (Iterator<Patch> iter = patches.iterator(); iter.hasNext();) {
            Patch otherPatch = iter.next();
            for (Turtle tempTurtle : otherPatch.turtlesHere()) {
              if (sourceSet.contains(tempTurtle)) {
                result.add(tempTurtle);
              }
            }
          }
        }
      } else {   //sourceSet is the entire set of turtles
        for (Patch p : patches) {
          for (Turtle t : p.turtlesHere()) {
            result.add(t);
          }
        }
      }

    }
    return new ArrayAgentSet
        (sourceSet.kind(), result.toArray(new Agent[result.size()]),
            world);
  }

  private boolean validateListEntry(Object entry) {
    if (entry instanceof LogoList) {
      LogoList entryList = (LogoList) entry;
      if (entryList.size() == 2 ) {
        for (Iterator<Object> iter = entryList.iterator(); iter.hasNext();) {
          if (!(iter.next() instanceof Double)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  private Set<Patch> getPatchesAtPoints(Context context, Agent agent, LogoList points) {
    // use LinkedHashSet here because we want fast lookup, but we also need
    // predictable ordering so runs are reproducible - ST 8/13/03
    LinkedHashSet<Patch> result =
        new LinkedHashSet<Patch>();
    for (Iterator<Object> it = points.iterator(); it.hasNext();) {
      LogoList entry = (LogoList) it.next();
      Double x = null;
      Double y = null;
      int j = 0;
      for (Iterator<Object> it2 = entry.iterator(); it2.hasNext();) {
        switch (j) {
          case 0:
            x = (Double) it2.next();
            break;
          case 1:
            y = (Double) it2.next();
            break;
          default:
            throw new EngineException(context, this,
                I18N.errorsJ().getN("org.nlogo.prim.etc._atpoints.invalidListOfPoints", Dump.logoObject(points)));
        }
        j++;
      }
      if (x == null || y == null) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._atpoints.invalidListOfPoints", Dump.logoObject(points)));
      }
      try {
        Patch patch = agent.getPatchAtOffsets(x.doubleValue(), y.doubleValue());
        if (patch != null) {
          result.add(patch);
        }
      } catch (AgentException e) {
        // do nothing
      }
    }
    return result;
  }

  @Override
  public Syntax syntax() {
    int left = Syntax.TurtlesetType() | Syntax.PatchsetType();
    int[] right = {Syntax.ListType()};
    int ret = Syntax.AgentsetType();
    return Syntax.reporterSyntax(left, right, ret, org.nlogo.api.Syntax.NormalPrecedence() + 2);
  }
}
