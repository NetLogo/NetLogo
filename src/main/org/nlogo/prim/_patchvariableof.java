// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _patchvariableof
    extends Reporter {
  public int vn = 0;

  public _patchvariableof(int vn) {
    this.vn = vn;
  }

  @Override
  public Object report(Context context) throws LogoException {
    Object agentOrSet = args[0].report(context);
    if (agentOrSet instanceof Agent) {
      Agent agent = (Agent) agentOrSet;
      if (agent.id == -1) {
        throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
      }
      try {
        return agent.getPatchVariable(vn);
      } catch (org.nlogo.api.AgentException ex) {
        throw new EngineException(context, this, ex.getMessage());
      }
    } else if (agentOrSet instanceof AgentSet) {
      AgentSet sourceSet = (AgentSet) agentOrSet;
      LogoListBuilder result = new LogoListBuilder();
      try {
        for (AgentIterator iter = sourceSet.shufflerator(context.job.random);
             iter.hasNext();) {
          result.add(iter.next().getPatchVariable(vn));
        }
      } catch (org.nlogo.api.AgentException ex) {
        throw new EngineException(context, this, ex.getMessage());
      }
      return result.toLogoList();
    } else {
      throw new org.nlogo.nvm.ArgumentTypeException
          (context, this, 0,
              Syntax.TurtleType() | Syntax.PatchType()
                  | Syntax.TurtlesetType() | Syntax.PatchsetType(),
              agentOrSet);
    }
  }

  @Override
  public String toString() {
    if (world != null) {
      return super.toString() + ":" + world.patchesOwnNameAt(vn);
    } else {
      return super.toString() + ":" + vn;
    }
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TurtleType() | Syntax.PatchType()
        | Syntax.TurtlesetType() | Syntax.PatchsetType()};
    int ret = Syntax.WildcardType();
    return Syntax.reporterSyntax(right, ret);
  }

  public Object report_1(Context context, Object agentOrSet) throws LogoException {
    if (agentOrSet instanceof Agent) {
      Agent agent = (Agent) agentOrSet;
      if (agent.id == -1) {
        throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
      }
      try {
        return agent.getPatchVariable(vn);
      } catch (org.nlogo.api.AgentException ex) {
        throw new EngineException(context, this, ex.getMessage());
      }
    } else if (agentOrSet instanceof AgentSet) {
      AgentSet sourceSet = (AgentSet) agentOrSet;
      LogoListBuilder result = new LogoListBuilder();
      try {
        for (AgentIterator iter = sourceSet.shufflerator(context.job.random);
             iter.hasNext();) {
          result.add(iter.next().getPatchVariable(vn));
        }
      } catch (org.nlogo.api.AgentException ex) {
        throw new EngineException(context, this, ex.getMessage());
      }
      return result.toLogoList();
    } else {
      throw new org.nlogo.nvm.ArgumentTypeException
          (context, this, 0,
              Syntax.TurtleType() | Syntax.PatchType()
                  | Syntax.TurtlesetType() | Syntax.PatchsetType(),
              agentOrSet);
    }
  }

  public Object report_2(Context context, AgentSet sourceSet)
      throws LogoException {
    LogoListBuilder result = new LogoListBuilder();
    try {
      for (AgentIterator iter = sourceSet.shufflerator(context.job.random);
           iter.hasNext();) {
        result.add(iter.next().getPatchVariable(vn));
      }
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    return result.toLogoList();
  }

  public Object report_3(Context context, Agent agent) throws LogoException {
    if (agent.id == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
    }
    try {
      return agent.getPatchVariable(vn);
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }
}
