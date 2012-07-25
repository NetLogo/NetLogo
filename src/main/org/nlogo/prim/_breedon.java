// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.ArrayList;
import java.util.List;

public final strictfp class _breedon
    extends Reporter {
  final String breedName;

  public _breedon(String breedName) {
    this.breedName = breedName;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TurtleType() | Syntax.PatchType() |
            Syntax.TurtlesetType() | Syntax.PatchsetType()},
            Syntax.TurtlesetType());
  }

  @Override
  public String toString() {
    return super.toString() + ":" + breedName;
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, args[0].report(context));
  }

  public AgentSet report_1(Context context, Object agentOrSet)
      throws LogoException {
    List<Turtle> resultList = new ArrayList<Turtle>();
    AgentSet breed = world.getBreed(breedName);
    if (agentOrSet instanceof Turtle) {
      Turtle turtle = (Turtle) agentOrSet;
      if (turtle.id == -1) {
        throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName()));
      }
      for (Turtle t : turtle.getPatchHere().turtlesHere()) {
        if (t.getBreed() == breed) {
          resultList.add(t);
        }
      }
    } else if (agentOrSet instanceof Patch) {
      for (Turtle turtle : ((Patch) agentOrSet).turtlesHere()) {
        if (turtle.getBreed() == breed) {
          resultList.add(turtle);
        }
      }
    } else if (agentOrSet instanceof AgentSet) {
      AgentSet sourceSet = (AgentSet) agentOrSet;
      if (sourceSet.kind() == AgentKindJ.Turtle()) {
        for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
          for (Turtle turtle : ((Turtle) iter.next()).getPatchHere().turtlesHere()) {
            if (turtle.getBreed() == breed) {
              resultList.add(turtle);
            }
          }
        }
      } else if (sourceSet.kind() == AgentKindJ.Patch()) {
        for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
          for (Turtle turtle : ((Patch) iter.next()).turtlesHere()) {
            if (turtle.getBreed() == breed) {
              resultList.add(turtle);
            }
          }
        }
      }
    } else {
      throw new ArgumentTypeException
          (context, this, 0,
              Syntax.TurtleType() | Syntax.PatchType() |
                  Syntax.TurtlesetType() | Syntax.PatchsetType(),
              agentOrSet);
    }
    return new ArrayAgentSet
      (AgentKindJ.Turtle(), resultList.toArray(new Turtle[resultList.size()]), world);
  }

  public AgentSet report_2(Context context, AgentSet sourceSet)
      throws LogoException {
    List<Turtle> resultList = new ArrayList<Turtle>();
    AgentSet breed = world.getBreed(breedName);
    if (sourceSet.kind() == AgentKindJ.Turtle()) {
      for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
        for (Turtle turtle : ((Turtle) iter.next()).getPatchHere().turtlesHere()) {
          if (turtle.getBreed() == breed) {
            resultList.add(turtle);
          }
        }
      }
    } else if (sourceSet.kind() == AgentKindJ.Patch()) {
      for (AgentIterator iter = sourceSet.iterator(); iter.hasNext();) {
        for (Turtle turtle : ((Patch) iter.next()).turtlesHere()) {
          if (turtle.getBreed() == breed) {
            resultList.add(turtle);
          }
        }
      }
    } else {
      throw new ArgumentTypeException
          (context, this, 0,
              Syntax.TurtleType() | Syntax.PatchType() |
                  Syntax.TurtlesetType() | Syntax.PatchsetType(),
              sourceSet);
    }
    return new ArrayAgentSet
      (AgentKindJ.Turtle(), resultList.toArray(new Turtle[resultList.size()]), world);
  }

  public AgentSet report_3(Context context, Agent agent)
      throws LogoException {
    List<Turtle> resultList = new ArrayList<Turtle>();
    AgentSet breed = world.getBreed(breedName);
    if (agent instanceof Turtle) {
      Turtle turtle = (Turtle) agent;
      if (turtle.id == -1) {
        throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName()));
      }
      for (Turtle t : turtle.getPatchHere().turtlesHere()) {
        if (t.getBreed() == breed) {
          resultList.add(t);
        }
      }
    } else if (agent instanceof Patch) {
      for (Turtle turtle : ((Patch) agent).turtlesHere()) {
        if (turtle.getBreed() == breed) {
          resultList.add(turtle);
        }
      }
    } else {
      throw new ArgumentTypeException
          (context, this, 0,
              Syntax.TurtleType() | Syntax.PatchType() |
                  Syntax.TurtlesetType() | Syntax.PatchsetType(),
              agent);
    }
    return new ArrayAgentSet
      (AgentKindJ.Turtle(), resultList.toArray(new Turtle[resultList.size()]), world);
  }

  public AgentSet report_4(Context context, Turtle turtle)
      throws LogoException {
    List<Turtle> resultList = new ArrayList<Turtle>();
    AgentSet breed = world.getBreed(breedName);
    if (turtle.id == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName()));
    }
    for (Turtle t : turtle.getPatchHere().turtlesHere()) {
      if (t.getBreed() == breed) {
        resultList.add(t);
      }
    }
    return new ArrayAgentSet
      (AgentKindJ.Turtle(), resultList.toArray(new Turtle[resultList.size()]), world);
  }

  public AgentSet report_5(Context context, Patch patch) {
    List<Turtle> resultList = new ArrayList<Turtle>();
    AgentSet breed = world.getBreed(breedName);
    for (Turtle turtle : patch.turtlesHere()) {
      if (turtle.getBreed() == breed) {
        resultList.add(turtle);
      }
    }
    return new ArrayAgentSet
      (AgentKindJ.Turtle(), resultList.toArray(new Turtle[resultList.size()]), world);
  }
}
