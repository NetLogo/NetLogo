// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.api.{ JobOwner, LogoException }
import org.nlogo.api.MersenneTwisterFast

class ConcurrentJob(owner: JobOwner, agentset: AgentSet, topLevelProcedure: Procedure,
                    address: Int, parentContext: Context, workspace: Workspace, random: MersenneTwisterFast)
extends Job(owner, agentset, topLevelProcedure, address, parentContext, workspace, random) {

  override def exclusive = false

  private[this] var contexts: Array[Context] = null

  private def initialize() {
    contexts = new Array[Context](agentset.count)
    var count = 0
    val iter = agentset.shufflerator(random)
    while(iter.hasNext) {
      newAgentJoining(iter.next(), count, address)
      count += 1
    }
  }

  def newAgentJoining(agent: Agent, initialCount: Int, address: Int) {
    var count = initialCount
    val context = new Context(
      this, agent, address,
      if (parentContext == null)
        new Activation(topLevelProcedure, null, 0)
      else parentContext.activation)
    if (count == -1) { // this whole -1 as a special value business is a bit kludgey - ST
      if (contexts == null)
        initialize()
      // special case -- called from JobManager.joinForeverButtons() the following code is very
      // slow, but it's a rare enough case that this seems fine for now... at present (October 2001)
      // we have no models that use this case -- pretty much only Termites has a turtle forever
      // button, and new termites aren't created on the fly - ST 10/23/01
      count = contexts.length
      val newContexts = new Array[Context](count + 1)
      System.arraycopy(contexts, 0, newContexts, 0, count)
      contexts = newContexts
    }
    contexts(count) = context
  }

  override def step() {
    if (contexts == null)
      initialize()
    // this is a very tight loop, so we pull as many calls out of the loop as possible
    val max = contexts.length
    var allContextsDone = true
    var context: Context = null
    var i = 0
    try while (i < max && state == Job.RUNNING) {
      context = contexts(i)
      if (context != null) {
        if (!context.finished) {
          if (!context.waiting)
            context.stepConcurrent()
          allContextsDone = false
        }
        else contexts(i) = null
      }
      i += 1
    }
    catch {
      case ex: LogoException =>
        finish()
        if (!Thread.currentThread.isInterrupted)
          context.runtimeError(ex)
        throw ex
      case ex: RuntimeException =>
        finish()
        context.runtimeError(ex)
        throw ex
    }
    if (state == Job.RUNNING && allContextsDone)
      finish()
  }

  override def finish() {
    super.finish()
    if (contexts != null) {
      val max = contexts.length
      var i = 0
      while(i < max) {
        val context = contexts(i)
        if (context != null)
          context.finished = true
        i += 1
      }
    }
  }

}
