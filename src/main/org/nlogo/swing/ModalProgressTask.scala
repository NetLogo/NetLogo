package org.nlogo.swing

import java.awt.{ BorderLayout, Frame }
import javax.swing.{ BorderFactory, JDialog, JLabel, JPanel, JProgressBar, SwingConstants }
import org.nlogo.awt.Utils.{ center, mustBeEventDispatchThread }

object ModalProgressTask {

  def apply(parent: java.awt.Frame, message: String, r: Runnable) {
    mustBeEventDispatchThread()

    // set up dialog
    val dialog = new javax.swing.JDialog(parent, true)
    dialog.setResizable(false)
    dialog.setUndecorated(true)

    // make components
    val label = new JLabel(message, SwingConstants.CENTER)
    val progressBar = new javax.swing.JProgressBar
    progressBar.setIndeterminate(true)

    // lay out dialog
    val panel = new JPanel
    panel.setBorder(
      BorderFactory.createEmptyBorder(15, 20, 15, 20))
    panel.setLayout(new BorderLayout(0, 8))
    panel.add(label, BorderLayout.NORTH)
    panel.add(progressBar, BorderLayout.SOUTH)
    dialog.getContentPane.setLayout(new BorderLayout)
    dialog.getContentPane.add(panel, BorderLayout.CENTER)
    dialog.pack()
    center(dialog, parent)

    // start the boss thread and show the dialog
    val boss = new Boss(dialog, r)
    boss.setPriority(Thread.MAX_PRIORITY)
    boss.start()
    dialog.setVisible(true)
  }

  private class Boss(dialog: JDialog, r: Runnable)
  extends Thread("ModalProgressTask#Boss") {
    override def run() {
      try {
        while (!dialog.isVisible)
        Thread.sleep(50)
        org.nlogo.awt.Utils.invokeAndWait(r)
      }
      catch {
        case _: InterruptedException => //ignore
      }
      finally {
        dialog.setVisible(false)
        dialog.dispose()
      }
    }
  }
}
