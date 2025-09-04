// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.AgentSet
import org.nlogo.api.JobOwner
import org.nlogo.api.MersenneTwisterFast

class ExclusiveJob(owner: JobOwner, agentset: AgentSet, topLevelProcedure: Procedure,
                   address: Int, parentContext: Context, workspace: Workspace, random: MersenneTwisterFast,
                   sibling: Boolean = false)
extends Job(owner, agentset, topLevelProcedure, address, parentContext, workspace, random) {

  override def exclusive = true

  // we are not suspendable. we run to the end and that's it
  override def step(): Unit = { throw new UnsupportedOperationException() }

  def run(): Unit = {
    // Note that this relies on shufflerators making a copy, which might change in a future
    // implementation. The cases where it matters are those where something happens that changes the
    // agentset as we're iterating through it, for example if we're iterating through all turtles
    // and one of them hatches; the hatched turtle must not be returned by the shufflerator.
    // - ST 12/5/05, 3/15/06
    val it = agentset.shufflerator(random)

    // normally, running an exclusive job creates a new agent context, which changes the values of `self` and `myself`.
    // the `sibling` flag allows primitives like `carefully` to get the required features of a new nested context while
    // preserving the values of `self` and `myself`. (Isaac B 9/4/25)
    val context = {
      if (sibling) {
        new Context(this, null, parentContext.myself, 0, null, workspace)
      } else {
        new Context(this, null, 0, null, workspace)
      }
    }

    // if the Job was created by Evaluator, then we may have no parent context - ST 7/11/06
    val runActivation =
      if (parentContext == null)
        new Activation(topLevelProcedure, null, address)
      else
        parentContext.activation
    context.agentBit = agentset.agentBit
    while (it.hasNext) {
      context.agent = it.next()
      context.activation = runActivation
      context.ip = address
      context.finished = false
      context.activation.binding = context.activation.binding.enterScope
      context.runExclusive()
      if (context.activation == runActivation) {
        context.activation.binding = context.activation.binding.exitScope
      }
    }
  }

  // used by Evaluator.MyThunk
  def callReporterProcedure() =
    new Context(this, agentset.iterator.next(), 0, null, workspace)
     .callReporterProcedure(new Activation(topLevelProcedure, null, 0))

}
