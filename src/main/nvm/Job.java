// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.JobOwner;

public abstract strictfp class Job {

  public static final byte RUNNING = 0;
  public static final byte DONE = 1;
  public static final byte REMOVED = 2;

  abstract boolean exclusive();

  public final JobOwner owner;                    // public for _updatemonitor
  public byte state = RUNNING;
  public boolean stopping = false;
  public boolean buttonTurnIsOver = false;
  public Object result;
  final int address;
  public final AgentSet agentset;
  public final Context parentContext;
  public final Procedure topLevelProcedure;

  public org.nlogo.util.MersenneTwisterFast random;

  Job(JobOwner owner,
      AgentSet agentset,
      Procedure topLevelProcedure,
      int address,
      Context parentContext,
      org.nlogo.util.MersenneTwisterFast random) {
    this.owner = owner;
    this.agentset = agentset;
    this.topLevelProcedure = topLevelProcedure;
    this.address = address;
    this.parentContext = parentContext;
    this.random = random;
  }

  public abstract void step();

  public void finish() {
    state = DONE;
    if (parentContext != null) {
      parentContext.waiting = false;
    }
  }

  public boolean isTurtleForeverButtonJob() {
    return topLevelProcedure != null
        && owner.isTurtleForeverButton();
  }

  public boolean isLinkForeverButtonJob() {
    return topLevelProcedure != null
        && owner.isLinkForeverButton();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(super.toString());
    buf.append("(owner: {" + owner + "}, state:");
    switch (state) {
      case RUNNING:
        buf.append("running");
        break;
      case DONE:
        buf.append("done");
        break;
      case REMOVED:
        buf.append("removed");
        break;
      default:
        throw new IllegalStateException
            ("unknown state: " + state);
    }
    buf.append(")");
    return buf.toString();
  }

}
