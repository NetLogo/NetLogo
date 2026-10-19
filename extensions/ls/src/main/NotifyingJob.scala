package org.nlogo.ls

import org.nlogo.agent.AgentSet
import org.nlogo.api.JobOwner
import org.nlogo.nvm.{ConcurrentJob, HaltException, Job, Procedure}
import org.nlogo.workspace.AbstractWorkspace

class NotifyingJob(override val notifyObject: AnyRef,
                   workspace: AbstractWorkspace,
                   owner: JobOwner,
                   agentSet: AgentSet,
                   procedure: Procedure)
extends ConcurrentJob(owner, agentSet, procedure, 0, null, workspace, owner.random)
with Notifying[AnyRef] {

  override def finish(): Unit = {
    super[ConcurrentJob].finish()
    super[Notifying].finish()
  }

  override def isFinished: Boolean = state == Job.REMOVED

  override def waitFor: AnyRef = try {
    waitForFinish()
    result
  } catch {
    case _: InterruptedException => throw new HaltException(true)
  }
}

class FakeNotifier[R](result: R) extends Notifying[R]{
  override def notifyObject: AnyRef = null
  override def finish(): Unit = {}
  override def isFinished: Boolean = true
  override def waitFor: R = result
}

trait Notifying[R] {
  def notifyObject: AnyRef

  def finish(): Unit = {
    notifyObject.synchronized(notifyObject.notify())
  }

  def isFinished: Boolean

  final def waitForFinish(): Unit = {
    while (!isFinished) {
      notifyObject.synchronized(notifyObject.wait(200))
    }
  }

  def waitFor: R

  def map[S](fn: R => S): Notifying[S] = MappedNotifying(this, fn)
}

case class MappedNotifying[A,B](base: Notifying[A], fn: A => B) extends Notifying[B] {
  override def notifyObject: AnyRef = base.notifyObject
  override def finish(): Unit = base.finish()
  override def isFinished: Boolean =  base.isFinished
  override def waitFor = fn(base.waitFor)
}
