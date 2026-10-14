// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ EventQueue, Frame, Toolkit }
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import javafx.application.Platform
import javafx.stage.{ DirectoryChooser, FileChooser }

import scala.util.control.NonFatal

/**
  * Shows the JavaFX file and directory choosers. On Windows these are intended to give
  * the modern system dialog (editable address bar, Quick Access, search) instead of the
  * legacy dialog that java.awt.FileDialog produces. JavaFX is already bundled with NetLogo.
  *
  * Both methods return:
  *   None              - this dialog is unavailable, the caller should use its old dialog
  *   Some(None)        - the user cancelled
  *   Some(Some(file))  - the user chose a file or directory
  *
  * Set -Dnetlogo.filedialog=awt to turn this off and get the previous behavior.
  */
private[swing] object FXFileDialog {

  @volatile private var failed = false
  private var toolkitStarted = false

  def enabled: Boolean =
    !failed &&
    System.getProperty("os.name", "").toLowerCase.startsWith("win") &&
    System.getProperty("netlogo.filedialog", "native").toLowerCase != "awt"

  def chooseFile(parent: Frame, title: String, mode: Int, file: String, directory: String): Option[Option[File]] = {
    if (!enabled) {
      None
    } else {
      attempt(parent) {
        val chooser = new FileChooser
        chooser.setTitle(title)

        val requested = Option(file).map(new File(_))

        requested.flatMap(f => Option(f.getParentFile)).filter(_.isDirectory)
          .orElse(Option(directory).map(new File(_)).filter(_.isDirectory))
          .foreach(chooser.setInitialDirectory)

        requested.foreach(f => chooser.setInitialFileName(f.getName))

        Option(
          if (mode == java.awt.FileDialog.SAVE)
            chooser.showSaveDialog(null)
          else
            chooser.showOpenDialog(null))
      }
    }
  }

  def chooseDirectory(parent: Frame, title: String, directory: String): Option[Option[File]] = {
    if (!enabled) {
      None
    } else {
      attempt(parent) {
        val chooser = new DirectoryChooser
        chooser.setTitle(title)

        Option(directory).map(new File(_)).filter(_.isDirectory).foreach(chooser.setInitialDirectory)

        Option(chooser.showDialog(null))
      }
    }
  }

  private def attempt(parent: Frame)(body: => Option[File]): Option[Option[File]] = {
    try {
      Some(withParentDisabled(parent)(onFXThread(() => body)))
    } catch {
      case NonFatal(t) =>
        // Fall back to the old dialogs for the rest of the session rather than
        // leaving the user unable to open or save files.
        failed = true
        t.printStackTrace()
        None
    }
  }

  // The JavaFX dialogs can't take a Swing frame as their owner, so to get modal behavior
  // the frame is disabled while the dialog is up.
  private def withParentDisabled[A](parent: Frame)(body: => A): A = {
    if (parent != null && EventQueue.isDispatchThread) {
      val wasEnabled = parent.isEnabled
      parent.setEnabled(false)
      try {
        body
      } finally {
        parent.setEnabled(wasEnabled)
        parent.toFront()
      }
    } else {
      body
    }
  }

  private def ensureToolkit(): Unit = synchronized {
    if (!toolkitStarted) {
      try {
        Platform.startup(() => ())
      } catch {
        case _: IllegalStateException => // the toolkit is already running (e.g. via a JFXPanel)
      }
      // A file chooser is not a Stage, so without this JavaFX may shut itself down
      // when the dialog closes.
      Platform.setImplicitExit(false)
      toolkitStarted = true
    }
  }

  // Runs body on the JavaFX thread and waits for its result. When called from the Swing event
  // thread, the wait is a secondary event loop so that the frame keeps repainting.
  private def onFXThread[A](body: () => A): A = {
    ensureToolkit()

    val outcome = new AtomicReference[Either[Throwable, A]]

    val task: Runnable = () => {
      val result: Either[Throwable, A] =
        try {
          Right(body())
        } catch {
          case t: Throwable => Left(t)
        }
      outcome.set(result)
    }

    if (EventQueue.isDispatchThread) {
      val loop = Toolkit.getDefaultToolkit.getSystemEventQueue.createSecondaryLoop()
      val fxTask: Runnable = () => {
        task.run()
        loop.exit()
        ()
      }
      // Posting from an event on the EDT guarantees that loop.enter() has started before
      // the task can possibly call loop.exit().
      EventQueue.invokeLater(() => Platform.runLater(fxTask))
      loop.enter()
    } else {
      val done = new CountDownLatch(1)
      Platform.runLater(() => {
        task.run()
        done.countDown()
      })
      done.await()
    }

    outcome.get match {
      case Right(value) => value
      case Left(t)      => throw t
    }
  }
}
