// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm, nvm.{ Command, Context, MutableLong }

import collection.mutable.WeakHashMap;

class _every extends Command with nvm.CustomAssembled {

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType, Syntax.CommandBlockType),
      true)

  override def toString =
    super.toString + ":+" + offset

  override def assemble(a: nvm.AssemblerAssistant) {
    a.add(this)
    a.block()
    a.resume()
  }

  override def perform(context: Context) {
    // current time is given in nanoseconds, so we must multiply by 1000000000 to get
    // the delay.  treat negative numbers the same as zero
    val delay =
      StrictMath.max(0, StrictMath.ceil(argEvalDoubleValue(context, 0) * 1000000000))
        .toLong
    // follow parentContext links to go backwards through the chain of "asks"
    // until we are at the top level "ask" (or button press etc.)
    val runTimes = workspace.lastRunTimes.getOrElseUpdate(context.job, WeakHashMap())
    // get the hash table for the agent; if there isn't one yet, make one
    val tempMap = runTimes.getOrElseUpdate(context.agent, WeakHashMap())
    val currentTime = System.nanoTime()
    // look up in the hash table lastRunTimes found in the job using the context.agent as
    // the lookup key to find the hash table of every calls by that agent
    tempMap.get(this) match {
      case None =>
        tempMap.put(this, MutableLong(currentTime))
        context.ip = next
      case Some(lastRunTime) =>
        if (currentTime >= lastRunTime.value + delay) {
          lastRunTime.value = currentTime
          context.ip = next
        }
        else
          context.ip = offset
    }
  }

}
