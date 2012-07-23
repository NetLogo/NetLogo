// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.agent.Observer;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _ask
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.AgentsetType() | Syntax.AgentType(),
            Syntax.CommandBlockType()},
            "OTPL", "?", true);
  }

  @Override
  public String toString() {
    return super.toString() + ":+" + offset;
  }

  @Override
  public void perform(Context context)
      throws LogoException {
    perform_1(context, args[0].report(context));
  }

  public void perform_1(Context context, Object target)
      throws LogoException {
    AgentSet agentset;
    if (target instanceof AgentSet) {
      agentset = (AgentSet) target;
      if (!(context.agent instanceof Observer)) {
        if (agentset == world.turtles()) {
          throw new EngineException
              (context, this, I18N.errorsJ().get("org.nlogo.prim.$common.onlyObserverCanAskAllTurtles"));
        }
        if (agentset == world.patches()) {
          throw new EngineException
              (context, this, I18N.errorsJ().get("org.nlogo.prim.$common.onlyObserverCanAskAllPatches"));
        }
      }
    } else if (target instanceof Agent) {
      Agent agent = (Agent) target;
      if (agent.id == -1) {
        throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
      }
      agentset = new ArrayAgentSet(agent.kind(), 1, false, world);
      agentset.add(agent);
    } else {
      throw new ArgumentTypeException(context, this, 0, Syntax.AgentsetType() | Syntax.AgentType(), target);
    }
    context.runExclusiveJob(agentset, next);
    context.ip = offset;
  }

  public void perform_2(Context context, AgentSet agentset)
      throws LogoException {
    if (!(context.agent instanceof Observer)) {
      if (agentset == world.turtles()) {
        throw new EngineException
            (context, this, I18N.errorsJ().get("org.nlogo.prim.$common.onlyObserverCanAskAllTurtles"));
      }
      if (agentset == world.patches()) {
        throw new EngineException
            (context, this, I18N.errorsJ().get("org.nlogo.prim.$common.onlyObserverCanAskAllPatches"));
      }
    }
    context.runExclusiveJob(agentset, next);
    context.ip = offset;
  }

  public void perform_3(Context context, Agent agent)
      throws LogoException {
    if (agent.id == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
    }
    AgentSet agentset = new ArrayAgentSet(agent.kind(), 1, false, world);
    agentset.add(agent);
    context.runExclusiveJob(agentset, next);
    context.ip = offset;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.block();
    a.done();
    a.resume();
  }
}
