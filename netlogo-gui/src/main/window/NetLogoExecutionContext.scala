// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.EventQueue.isDispatchThread

import scala.concurrent.ExecutionContext

import org.nlogo.awt.EventQueue

object NetLogoExecutionContext {
  implicit def backgroundExecutionContext: ExecutionContext =
    ExecutionContext.global
}

object SwingUnlockedExecutionContext extends ExecutionContext {
  def execute(runnable: Runnable): Unit     = {
    if (isDispatchThread)
      runnable.run
    else
      EventQueue.invokeLater(runnable)
  }

  def reportFailure(cause: Throwable): Unit =
    ExecutionContext.defaultReporter(cause)
}

class LockedExecutionContext(lock: Object, delegate: ExecutionContext) extends ExecutionContext {
  def execute(runnable: Runnable): Unit = {
    delegate.execute(new Runnable {
      override def run(): Unit = {
        lock.synchronized {
          runnable.run()
        }
      }
    })
  }

  def reportFailure(cause: Throwable): Unit =
    delegate.reportFailure(cause)

  override def prepare(): ExecutionContext =
    new LockedExecutionContext(lock, delegate)
}

class LockedBackgroundExecutionContext(lock: Object)
  extends LockedExecutionContext(lock, NetLogoExecutionContext.backgroundExecutionContext)

class SwingLockedExecutionContext(lock: Object)
  extends LockedExecutionContext(lock, SwingUnlockedExecutionContext)
