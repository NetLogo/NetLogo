package org.nlogo.ls

import javax.swing.SwingUtilities

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object UnlockAndBlock {
  val swingExecutionContext: ExecutionContext = ExecutionContext.fromExecutor(
    (command: Runnable) => SwingUtilities.invokeLater(command)
  )

  def onEDT[R](unlock: AnyRef)(fn: => R): R = UnlockAndBlock.run(swingExecutionContext, unlock)(fn)
  /**
   * Runs `fn` in a Future and waits for it to return while keeping `unlock`
   * unlocked via `unlock.wait`.
   * The primary purpose of this is to allow you to block on code in other
   * threads that may need to lock on this model's world. This includes
   * anything that may end up blocking on the EDT doing something.
   **/
  def run[R](executionContext: ExecutionContext, unlock: AnyRef)(fn: => R): R = {
    implicit val ec: ExecutionContext = executionContext
    val future = Future {
      try fn finally unlock.synchronized {
        unlock.notifyAll()
      }
    }
    try {
      while (!future.isCompleted) {
        unlock.synchronized {
          unlock.wait(0,1)
        }
        if (Thread.currentThread.isInterrupted) throw new InterruptedException()
      }
      future.value.map {
        case Success(v) => v
        case Failure(e) => throw e
      }.get
    } catch {
      case _: InterruptedException =>
        Thread.currentThread.interrupt()
        throw new org.nlogo.nvm.HaltException(false)
    }
  }
}

