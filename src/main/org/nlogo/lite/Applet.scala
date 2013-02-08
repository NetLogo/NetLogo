// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.awt.Hierarchy.getFrame
import org.nlogo.awt.EventQueue.invokeLater
import org.nlogo.window.{ RuntimeErrorDialog, VMCheck }
import org.nlogo.util.Exceptions
import javax.swing.{ JApplet, JFrame, JPanel, JOptionPane, JLabel }

class Applet extends JApplet
with org.nlogo.window.Events.CompiledEvent.Handler {

  override def init() {
    VMCheck.detectBadJVMs()
    invokeLater(new Runnable() {
      override def run() { init2(); go(null) } })
  }

  override def destroy() {
    // Once we've been notified we're being shut down, we don't want any stray error dialog to come
    // popping up.  see #960 - ST 6/30/10
    RuntimeErrorDialog.deactivate()
  }

  var panel: AppletPanel = _

  def init2() {
    val mouseAdapter =
     new java.awt.event.MouseAdapter() {
       override def mouseClicked(e: java.awt.event.MouseEvent) {
         Applet.this.getAppletContext().showDocument(
           new java.net.URL("http://ccl.northwestern.edu/netlogo/"), "_blank")
       }}
    panel = new AppletPanel(getFrame(this), mouseAdapter, true) {
      override def getFileURL(filename: String) =
        new java.net.URL(Applet.this.getCodeBase(), filename)
    }
    add(panel)
    // where our resources are
    panel.setPrefix(getCodeBase)
  }

  def go(path: String) {
    import org.nlogo.util.Utils.{ unescapeSpacesInURL, url2String }
    Exceptions.handling(classOf[java.io.IOException], classOf[RuntimeException]) {
      try {
        var name = getParameter("DefaultModel")
        if(getCodeBase.toString.startsWith("file:") &&
           System.getProperty("os.name").startsWith("Windows"))
          name = unescapeSpacesInURL(name)
        val modelURL = new java.net.URL(getCodeBase, name)
        panel.setPrefix(modelURL)
        val source = url2String(modelURL.toString)
        panel.openFromSource(name, name, source)
        // not really sure why the invokeLater seems to be necessary here - ST 8/31/04
        invokeLater(new Runnable() {
          override def run() { requestFocus() } })
      }
      catch {
        case ex: java.io.FileNotFoundException =>
          showModelNotFoundDialog(ex.getMessage)
        case _: org.nlogo.window.InvalidVersionException =>
          showInvalidVersionDialog()
      }
    }
  }

  override def requestFocus() {
    for(p <- Option(panel))
      p.requestFocus()
  }

  def handle(e: org.nlogo.window.Events.CompiledEvent) {
    for(error <- Option(e.error)) {
      val ownerName =
        Option(e.sourceOwner).map(o => o.classDisplayName + ": ").getOrElse("")
      val msg = ownerName + error.getMessage
      new JPanel {
        add(new JLabel(msg))
        setBackground(java.awt.Color.white)
        Applet.this.add(this)
      }
    }
  }

  def showModelNotFoundDialog(name: String) {
    val message = "Model file not found:\n" + name + "\n\n" +
      "The model should be in the same directory as the HTML\n" +
      "file containing the applet.  (Or, you can modify the defaultModel\n" +
      "attribute of the APPLET tag to point to a different location.)\n"
    val pane = new JOptionPane(message)
    val dialog = pane.createDialog(new JFrame, "Model File Not Found")
    dialog.setVisible(true)
  }

  def showInvalidVersionDialog() {
    val message = "The file is not a valid NetLogo model file."
    val pane = new JOptionPane(message)
    val dialog = pane.createDialog(new JFrame, "Model File Not Found")
    dialog.setVisible(true)
  }

}
