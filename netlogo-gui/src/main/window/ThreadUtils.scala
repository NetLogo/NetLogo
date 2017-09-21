// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{CommandRunnable, LogoException, ReporterRunnable}
import org.nlogo.nvm.HaltException

object ThreadUtils {
  /// asking for stuff to happen on the event thread
  val DO_NOTHING = new CommandRunnable() {def run() {}}

  @throws(classOf[LogoException])
  def waitForQueuedEvents(workspace: GUIWorkspaceScala): Unit = {waitFor(workspace, DO_NOTHING)}

  def waitFor(workspace: GUIWorkspaceScala, runnable: Runnable): Unit = {
    try waitForResult(workspace, reporter(runnable.run _))
    catch {
      case ex: HaltException => org.nlogo.api.Exceptions.ignore(ex)
      case ex: LogoException => throw new IllegalStateException(ex)
    }
  }

  private def reporter(fn: () => Unit) = new ReporterRunnable[Object]() {
    def run() = { fn(); Boolean.box(true) }
  }

  @throws(classOf[LogoException])
  def waitFor(workspace: GUIWorkspaceScala, runnable: CommandRunnable) {
    waitForResult(workspace, reporter(runnable.run _))
  }

  private class Result[T] {
    @volatile var done = false // NOPMD pmd doesn't like 'volatile'
    var value: T = _
    var ex: Exception = null
  }

  @throws(classOf[LogoException])
  def waitForResult[T](workspace: GUIWorkspaceScala, runnable: ReporterRunnable[T]) = {
    val result = new Result[T]()
    // in order to wait for the event thread without deadlocking,
    // we need to give up our lock on World by calling wait()
    // otherwise we deadlock when View.paintComponent()
    // (or other code on the event thread) tries to lock World
    // - ST 8/13/03,8/16/03
    try {
      org.nlogo.awt.EventQueue.invokeLater(new Runnable() {
        def run() {
          try result.value = runnable.run()
          catch {
            case ex: LogoException => result.ex = ex
            case ex: RuntimeException => result.ex = ex
          }
          finally {
            result.done = true
            workspace.world.synchronized {workspace.world.notifyAll()}
          }
        }
      })
      // the while loop here is necessary because
      // notify() might actually get called before
      // we call wait() for the first time,so we need
      // to wake up from time to time and check the done
      // flag to see if we missed our notification
      // - ST 8/13/03
      while (!result.done) {
        workspace.world.synchronized {workspace.world.wait(50)}
        if (Thread.currentThread.isInterrupted)
          throw new InterruptedException()
      }
      if(result.ex != null)
        throw result.ex
      result.value
    }
    catch {
      case ex: InterruptedException =>
        Thread.currentThread.interrupt()
        throw new org.nlogo.nvm.HaltException(false)
    }
  }
}
