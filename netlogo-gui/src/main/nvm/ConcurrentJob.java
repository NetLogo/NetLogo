// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.JobOwner;
import org.nlogo.api.LogoException;

public strictfp class ConcurrentJob
    extends Job {

  public ConcurrentJob(JobOwner owner,
                       AgentSet agentset,
                       Procedure topLevelProcedure,
                       int address,
                       Context parentContext,
                       Workspace workspace,
                       org.nlogo.api.MersenneTwisterFast random) {
    super(owner, agentset, topLevelProcedure, address, parentContext, workspace, random);
  }

  private Context[] contexts;

  @Override
  boolean exclusive() {
    return false;
  }

  private void initialize() {
    contexts = new Context[agentset.count()];
    int count = 0;
    for (AgentSet.Iterator iter = agentset.shufflerator(random);
         iter.hasNext();) {
      Agent agent = iter.next();
      newAgentJoining(agent, count++, address);
    }
  }

  public void newAgentJoining(Agent agent, int count, int address) {
    Context context =
        new Context(this, agent,
            address,
            parentContext == null
                ? new Activation(topLevelProcedure, null, 0)
                : parentContext.activation, workspace);
    if (count == -1) // this whole -1 as a special value business is a bit kludgey - ST
    {
      if (contexts == null) {
        initialize();
      }
      // special case -- called from JobManager.joinForeverButtons()
      // the following code is very slow, but it's a rare enough
      // case that this seems fine for now... at present (October 2001)
      // we have no models that use this case -- pretty much only Termites has
      // a turtle forever button, and new termites aren't created on
      // the fly - ST 10/23/01
      count = contexts.length;
      Context[] newContexts = new Context[count + 1];
      System.arraycopy(contexts, 0, newContexts, 0, count);
      contexts = newContexts;
    }
    contexts[count] = context;
  }

  @Override
  public void step()
      throws LogoException {
    if (contexts == null) {
      initialize();
    }
    // this is a very tight loop, so we pull as many calls
    // out of the loop as possible
    int max = contexts.length;
    boolean allContextsDone = true;
    Context context = null;
    try {
      for (int i = 0; i < max && state == RUNNING; i++) {
        context = contexts[i];
        if (context != null) {
          if (!context.finished) {
            if (!context.waiting) {
              context.stepConcurrent();
            }
            allContextsDone = false;
          } else {
            contexts[i] = null;
          }
        }
      }
    } catch (LogoException ex) {
      finish();
      if (!Thread.currentThread().isInterrupted()) {
        context.runtimeError(ex);
      }
      throw ex;
    } catch (RuntimeException ex) {
      finish();
      context.runtimeError(ex);
      throw ex;
    }
    if (state == RUNNING && allContextsDone) {
      finish();
    }
  }

  @Override
  public void finish() {
    super.finish();
    if (contexts != null) {
      int max = contexts.length;
      for (int i = 0; i < max; i++) {
        Context context = contexts[i];
        if (context != null) {
          context.finished = true;
        }
      }
    }
  }

}
