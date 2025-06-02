// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Frame
import javax.swing.JDialog

import org.nlogo.awt.EventQueue

import scala.concurrent.{ Await, Promise }
import scala.concurrent.duration.{ Duration, MILLISECONDS }

trait ModalProgress {
  def showModalProgressPanel(message: String): Unit
  def hideModalProgressPanel(): Unit
}

object ModalProgressTask {
  def display(parent: Frame, message: String, perform: ModalProgress => Unit): Unit = {
    parent match {
      case mp: ModalProgress =>
        EventQueue.invokeLater { () => perform(mp) }

        mp.showModalProgressPanel(message)

      case _ =>
        throw new IllegalStateException("The modal progress panel can't be displayed on the specified frame.")
    }
  }

  def onUIThread(parent: Frame, message: String, r: Runnable): Unit = {
    display(parent, message, { mp =>
      try {
        r.run()
      } catch {
        case _: InterruptedException => // ignore
      } finally {
        mp.hideModalProgressPanel()
      }
    })
  }

  def runForResultOnBackgroundThread[A, B](parent: Frame, message: String, uiThreadData: () => A, runInBackground: A => B): B = {
    parent match {
      case mp: ModalProgress =>
        val completionPromise = Promise[B]()

        val worker = new Worker[A, B](mp, uiThreadData(), runInBackground, completionPromise)

        worker.setPriority(Thread.MAX_PRIORITY)
        worker.start()

        mp.showModalProgressPanel(message)

        // if this is being run, it means that the panel should have been hidden
        // and therefore the promise completed.
        Await.result(completionPromise.future, Duration(10, MILLISECONDS))

      case _ =>
        throw new IllegalStateException("The modal progress panel can't be displayed on the specified frame.")
    }
  }

  private class Worker[A, B](mp: ModalProgress, data: A, runInBackground: A => B, promise: Promise[B]) extends
    Thread("ModalProgressTask#Worker") {
    override def run(): Unit = {
      try {
        promise.success(runInBackground(data))
      } catch {
        case e: Exception => promise.failure(e)
      } finally {
        mp.hideModalProgressPanel()
      }
    }
  }
}
