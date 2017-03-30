// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.{ AgentIterator, AgentSet }
import org.nlogo.api.JobOwner
import org.nlogo.api.MersenneTwisterFast

class DummyJobOwner(val random: MersenneTwisterFast) extends JobOwner {
  def displayName: String = "Job Owner" // TODO: we may want another button
  def isButton: Boolean = true // TODO: our only owners at this point are buttons
  def isCommandCenter: Boolean = false
  def isLinkForeverButton: Boolean = false
  def isTurtleForeverButton: Boolean = false
  def ownsPrimaryJobs: Boolean = true

  def classDisplayName: String = "Button"
  def headerSource: String = ""
  def innerSource: String = ""
  def innerSource_=(s: String): Unit = {}
  def kind: org.nlogo.core.AgentKind = org.nlogo.core.AgentKind.Observer
  def source: String = ""
}

class SuspendableJob(
  suspendedState: Option[(Context, AgentIterator)], parentActivation: Activation, forever: Boolean, agentset: AgentSet, topLevelProcedure: Procedure,
  address: Int, parentContext: Context, random: MersenneTwisterFast)
  extends Job(new DummyJobOwner(random), agentset, topLevelProcedure, address, parentContext, random)
  with org.nlogo.internalapi.SuspendableJob {

  def this(
  suspendedState: Option[(Context, AgentIterator)], forever: Boolean, agentset: AgentSet, topLevelProcedure: Procedure,
  address: Int, parentContext: Context, random: MersenneTwisterFast) =
    this(suspendedState, {
      // if the Job was created by Evaluator, then we may have no parent context - ST 7/11/06
      if (parentContext == null)
        new Activation(topLevelProcedure, null, address)
      else
        parentContext.activation
    }, forever, agentset, topLevelProcedure, address, parentContext, random)

  def this(agentset: AgentSet, topLevelProcedure: Procedure,
    address: Int, parentContext: Context, random: MersenneTwisterFast) =
      this(None, false, agentset, topLevelProcedure, address, parentContext, random)

  def this(agentset: AgentSet, forever: Boolean, topLevelProcedure: Procedure,
    address: Int, parentContext: Context, random: MersenneTwisterFast) =
      this(None, forever, agentset, topLevelProcedure, address, parentContext, random)

  override def exclusive = true

  // we are not suspendable. we run to the end and that's it
  override def step() { throw new UnsupportedOperationException() }

  @scala.annotation.tailrec
  private final def runShuffled(stepsRemaining: Int, agentIterator: AgentIterator): Option[SuspendableJob] = {
    if (agentIterator.hasNext) {
      val agent = agentIterator.next()
      val c = new Context(this, null, 0, null)
      c.agent = agent
      c.agentBit = agentset.agentBit
      c.activation = parentActivation
      c.ip = address
      c.finished = false
      c.activation.binding = c.activation.binding.enterScope()
      c.runFor(stepsRemaining) match {
        case Left(continueContext) =>
          Some(new SuspendableJob(Some((c, agentIterator)), parentActivation, forever, agentset,
            topLevelProcedure, address, parentContext, random))
        case Right(completedSteps) =>
          if (c.activation == parentActivation) {
            c.activation.binding = c.activation.binding.exitScope()
          }
          runShuffled(stepsRemaining - completedSteps, agentIterator)
      }
    } else {
      if (forever) {
        Some(new SuspendableJob(None, parentActivation, true, agentset, topLevelProcedure,
          address, parentContext, random))
      } else {
        state = Job.DONE
        None
      }
    }
  }

  def runFor(steps: Int): Option[SuspendableJob] = {
    state = Job.RUNNING
    // Note that this relies on shufflerators making a copy, which might change in a future
    // implementation. The cases where it matters are those where something happens that changes the
    // agentset as we're iterating through it, for example if we're iterating through all turtles
    // and one of them hatches; the hatched turtle must not be returned by the shufflerator.
    // - ST 12/5/05, 3/15/06
    suspendedState match {
      case Some((ctx, it)) =>
        ctx.runFor(steps) match {
          case Left(continueContext) =>
            Some(new SuspendableJob(Some((ctx, it)), parentActivation, forever, agentset,
              topLevelProcedure, address, parentContext, random))
          case Right(completedSteps) =>
            if (ctx.activation == parentActivation)
              ctx.activation.binding = ctx.activation.binding.exitScope()
            runShuffled(steps - completedSteps, it)
        }
      case None =>
        val it = agentset.shufflerator(random)
        runShuffled(steps, it)
    }
  }

  // used by Evaluator.MyThunk
  def callReporterProcedure() =
    new Context(this, agentset.iterator.next(), 0, null)
     .callReporterProcedure(new Activation(topLevelProcedure, null, 0))

}
