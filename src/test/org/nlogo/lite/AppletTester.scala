// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.awt.EventQueue.invokeLater
import org.nlogo.window.{ Event, VMCheck }
import org.nlogo.workspace.AbstractWorkspace

object AppletTester {
  def main(args: Array[String]) {
    System.setProperty("apple.awt.graphics.UseQuartz", "true")
    System.setProperty("apple.awt.showGrowBox", "true")
    AbstractWorkspace.isApplet(false)
    VMCheck.detectBadJVMs()
    val (eventsArgs, otherArgs) = args.partition(_ == "--events")
    Event.logEvents = eventsArgs.nonEmpty
    val path = otherArgs.headOption.getOrElse(chooseModel)
    invokeLater(new Runnable() { def run() {
      open(path) } })
  }
  def chooseModel = {
    val dialog = new java.awt.FileDialog(
      null: java.awt.Frame, "Open: NetLogo Model", java.awt.FileDialog.LOAD)
    dialog.setVisible(true)
    if(dialog.getDirectory() == null)
      System.exit(0)
    dialog.getDirectory + dialog.getFile
  }
  def open(path: String) {
    val frame = new javax.swing.JFrame("NetLogo Model")
    val panel = new AppletPanel(frame, new java.awt.event.MouseAdapter() { }, false) {
      override def getFileURL(filename: String) =
        AbstractWorkspace.toURL(new java.io.File(filename))
      override def getInsets =
        new java.awt.Insets(5, 5, 5, 5)
    }
    frame.setResizable(false)
    frame.addWindowListener(
      new java.awt.event.WindowAdapter() {
        override def windowClosing(e: java.awt.event.WindowEvent) {
          frame.setVisible(false)
          frame.dispose()
          open(chooseModel)
      } })
    import java.awt.BorderLayout
    frame.getContentPane.setLayout(new BorderLayout)
    frame.getContentPane.add(panel, BorderLayout.CENTER)
    frame.pack() // create peers, otherwise go() will fail
    panel.setAdVisible(false)
    val name = new java.io.File(path).getName
    val source = org.nlogo.api.FileIO.file2String(path)
    val modelPath = new java.io.File(path).getAbsolutePath()
    panel.openFromSource(name, modelPath, source)
    frame.pack() // now that InterfacePanel knows its preferred size
    frame.setVisible(true)
  }
}
