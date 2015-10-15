// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentException;
import org.nlogo.api.Dump;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.*;

public final strictfp class _atpoints
    extends Reporter {
  @Override
  public Object report(final Context context) {
    // part 1: get arguments, context.checked validity
    AgentSet sourceSet = argEvalAgentSet(context, 0);
    List<Agent> result = new ArrayList<Agent>();
    LogoList points = argEvalList(context, 1);
    for (Object elt : points.toJava()) {
      if (!validateListEntry(elt)) {
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
    return AgentSet.fromArray(
      sourceSet.kind(), result.toArray(new Agent[result.size()]));
  }

  private boolean validateListEntry(Object entry) {
    if (entry instanceof LogoList) {
      LogoList entryList = (LogoList) entry;
      if (entryList.size() == 2 ) {
        for (Object obj : entryList.toJava()) {
          if (!(obj instanceof Double)) {
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
    for (Iterator<Object> it = points.javaIterator(); it.hasNext();) {
      LogoList entry = (LogoList) it.next();
      Double x = null;
      Double y = null;
      int j = 0;
      for (Iterator<Object> it2 = entry.javaIterator(); it2.hasNext();) {
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

}
