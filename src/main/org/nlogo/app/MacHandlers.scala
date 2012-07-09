// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import net.roydesign.event.ApplicationEvent
import net.roydesign.mac.MRJAdapter
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.Implicits.thunk2actionListener
import org.nlogo.api.I18N

object MacHandlers {
  private var app: App = null
  private var openMeLater: String = null
  // init() should be called very, very early (I'm guessing that the rule is, before the AWT
  // initializes) - ST 11/13/03
  def init() {
    MRJAdapter.addOpenDocumentListener(
      new java.awt.event.ActionListener() {
        override def actionPerformed(e: java.awt.event.ActionEvent) {
          doOpen(e.asInstanceOf[ApplicationEvent].getFile.getAbsolutePath)
        }})
    MRJAdapter.addAboutListener(() => app.showAboutWindow())
    MRJAdapter.addQuitApplicationListener{() =>
      try app.fileMenu.quit()
      catch { case e: UserCancelException => } // ignore
    }
  }
  def ready(app: App) {
    this.app = app
    if(openMeLater != null)
      doOpen(openMeLater)
  }
  private def doOpen(path: String) {
    if(app == null)
      openMeLater = path
    else try {
      org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
      app.fileMenu.offerSave()
      app.open(path)
    }
    catch {
      case ex: org.nlogo.awt.UserCancelException =>
        org.nlogo.util.Exceptions.ignore(ex)
      case ex: java.io.IOException =>
        javax.swing.JOptionPane.showMessageDialog(
          app.frame, ex.getMessage,
          I18N.gui.get("common.messages.error"), javax.swing.JOptionPane.ERROR_MESSAGE)
    }
  }
}
