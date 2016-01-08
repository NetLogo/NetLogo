// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.BorderLayout
import javax.swing.{ BorderFactory, JDialog, JLabel, JPanel, SwingConstants }
import org.nlogo.awt.EventQueue.mustBeEventDispatchThread
import org.nlogo.awt.Positioning.center
import org.nlogo.api.Version

object ModalProgressTask {
  val isMac = System.getProperty("os.name").startsWith("Mac")

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

    // avoid depending on api.Version
    def is3D =
      try java.lang.Boolean.getBoolean("org.nlogo.is3d")
      catch {
        case e: Exception => false
      }

    // This is a workaround for the menu getting grayed out on macs in 3D mode
    // See issue #47  FD 6/12/14
    if(isMac && is3D) {
      val threedFixBoss = new Boss(dialog, new Runnable() { override def run() {} })
      threedFixBoss.setPriority(Thread.MAX_PRIORITY)
      threedFixBoss.start()
      dialog.setVisible(true)
    }
  }

  private class Boss(dialog: JDialog, r: Runnable)
  extends Thread("ModalProgressTask#Boss") {
    override def run() {
      try {
        while (!dialog.isVisible) {
          Thread.sleep(50)
        }
        dialog.repaint() // ensure repaint before blocking on the runnable -- NP 2015-04-24
        org.nlogo.awt.EventQueue.invokeAndWait(r)
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
