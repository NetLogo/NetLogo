package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.I18N;
import org.nlogo.api.I18NJava;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;
import org.nlogo.nvm.Context;

public final strictfp class _breedvariableof
    extends Reporter {
  public String name;

  public _breedvariableof(String name) {
    this.name = name;
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_TURTLE | Syntax.TYPE_TURTLESET},
            Syntax.TYPE_WILDCARD);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + name;
  }

  @Override
  public Object report(Context context) throws LogoException {
    Object agentOrSet = args[0].report(context);
    if (agentOrSet instanceof Agent) {
      Agent agent = (Agent) agentOrSet;
      if (agent.id == -1) {
        throw new EngineException(context, this,
          I18NJava.errors().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
      }
      try {
        return agent.getBreedVariable(name);
      } catch (org.nlogo.api.AgentException ex) {
        throw new EngineException(context, this, ex.getMessage());
      }
    } else if (agentOrSet instanceof AgentSet) {
      AgentSet sourceSet = (AgentSet) agentOrSet;
      LogoListBuilder result = new LogoListBuilder();
      try {
        for (AgentSet.Iterator iter = sourceSet.shufflerator(context.job.random);
             iter.hasNext();) {
          result.add(iter.next().getBreedVariable(name));
        }
      } catch (org.nlogo.api.AgentException ex) {
        throw new EngineException(context, this, ex.getMessage());
      }
      return result.toLogoList();
    } else {
      throw new org.nlogo.nvm.ArgumentTypeException
          (context, this, 0,
              Syntax.TYPE_TURTLESET | Syntax.TYPE_TURTLE,
              agentOrSet);
    }
  }
}
