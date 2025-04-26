// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Frame, Window }
import javax.swing.JDialog

import org.nlogo.awt.EventQueue, EventQueue.mustBeEventDispatchThread

import scala.concurrent.{ Await, Promise }
import scala.concurrent.duration.{ Duration, MILLISECONDS }

object ModalProgressTask {
  def display(parent: Frame, message: String, perform: JDialog => Unit): Unit = {
    mustBeEventDispatchThread()

    val dialog = new ModalProgressDialog(parent, message)
    EventQueue.invokeLater { () => perform(dialog) }

    dialog.setVisible(true)
  }

  def onUIThread(parent: Frame, message: String, r: Runnable): Unit = {
    display(parent, message, { dialog =>
      try {
        r.run()
      } catch {
        case _: InterruptedException => // ignore
      } finally {
        dialog.setVisible(false)
        dialog.dispose()
      }
    })
  }

  def onBackgroundThreadWithUIData[A](parent: Frame, message: String, uiThreadData: () => A, runInBackground: A => Unit) = {
    runForResultOnBackgroundThread[A, Unit](parent, message, (dialog: JDialog) => uiThreadData(), runInBackground)
  }

  def runForResultOnBackgroundThread[A, B](parent: Frame, message: String, uiThreadData: JDialog => A, runInBackground: A => B): B = {
    mustBeEventDispatchThread()

    val dialog = new ModalProgressDialog(parent, message)

    val completionPromise = Promise[B]()

    EventQueue.invokeLater { () =>
      val data = uiThreadData(dialog)

      val worker = new Worker[A, B](dialog, data, runInBackground, completionPromise)
      worker.setPriority(Thread.MAX_PRIORITY)
      worker.start()
    }

    dialog.setVisible(true)

    // if this is being run, it means that the dialog should have been hidden
    // and therefore the promise completed.
    Await.result(completionPromise.future, Duration(10, MILLISECONDS))
  }

  private class Worker[A, B](window: Window, data: A, runInBackground: A => B, promise: Promise[B]) extends
    Thread("ModalProgressTask#Worker") {
    override def run(): Unit = {
      try {
        promise.success(runInBackground(data))
      } catch {
        case e: Exception => promise.failure(e)
      } finally {
        EventQueue.invokeLater { () =>
          window.setVisible(false)
          window.dispose()
        }
      }
    }
  }
}
