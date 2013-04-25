// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.MutableLong;

import java.util.WeakHashMap;

public final strictfp class _every
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType(), Syntax.CommandBlockType()},
            true);
  }

  @Override
  public String toString() {
    return super.toString() + ":+" + offset;
  }

  @Override
  public void perform(final Context context) {
    //the current time given in nanoseconds so we must multiply by 1000000000 to get the delay
    //and we treat negative numbers the same as zero
    long delay =
        (long) StrictMath.max(0,
            StrictMath.ceil
                (argEvalDoubleValue(context, 0) * 1000000000));

    // follow parentContext links to go backwards through the chain of "asks"
    // until we are at the top level "ask" (or button press etc.)
    WeakHashMap<Agent, WeakHashMap<Command, MutableLong>> runTimes =
        workspace.lastRunTimes().get(context.job);

    // look at the hash table for the job; if there isn't one yet, make one
    if (runTimes == null) {
      runTimes = new WeakHashMap<Agent, WeakHashMap<Command, MutableLong>>();
      workspace.lastRunTimes().put(context.job, runTimes);
    }

    // get the hash table for the agent; if there isn't one yet, make one
    WeakHashMap<Command, MutableLong> tempMap = runTimes.get(context.agent);
    if (tempMap == null) {
      tempMap = new WeakHashMap<Command, MutableLong>();
      runTimes.put(context.agent, tempMap);
    }

    //look up in the hash table lastRunTimes found in the job using the context.agent as
    //the lookup key to find the hash table of every calls by that agent
    MutableLong lastRunTime = tempMap.get(this);
    long currentTime = System.nanoTime();
    if (lastRunTime == null) {
      tempMap.put(this, new MutableLong(currentTime));
      // execute the commands in the block
      context.ip = next;
    } else if (currentTime >= lastRunTime.value() + delay) {
      lastRunTime.value_$eq(currentTime);
      // execute the commands in the block
      context.ip = next;
    } else {
      // skip over the block
      context.ip = offset;
    }
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.block();
    a.resume();
  }
}
