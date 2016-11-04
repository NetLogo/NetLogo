// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.swing.Implicits._
import org.nlogo.window.{ ClientAppInterface, EditorFactory }
import java.awt.BorderLayout
import org.nlogo.swing.{ModalProgressTask, OptionDialog}
import org.nlogo.awt.{ Hierarchy, Images, Positioning, EventQueue }
import org.nlogo.hubnet.connection.Ports
import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import javax.swing.{WindowConstants, JFrame}

/**
 * The HubNet client.
 */
object ClientApp {
  private var localClientIndex = 0

  // called by App.main()
  def mainHelper(args: Array[String], editorFactory: EditorFactory, workspace: CompilerServices) {
    try {
      val app = new ClientApp()
      org.nlogo.swing.Utils.setSystemLookAndFeel()

      var isRoboClient = false
      var waitTime = 500
      var userid = ""
      var hostip = ""
      var port = Ports.DEFAULT_PORT_NUMBER

      for (i <- 0 until args.length) {
        if (args(i).equalsIgnoreCase("--robo")) {
          isRoboClient = true
          if (i + 1 < args.length) {
            try waitTime = args(i + 1).toLong.toInt
            catch {
              // it is not the optional wait time parameter
              case nfe: NumberFormatException => org.nlogo.api.Exceptions.ignore(nfe)
            }
          }
        }
        else if (args(i).equalsIgnoreCase("--id")) userid = args(i + 1)
        else if (args(i).equalsIgnoreCase("--ip")) hostip = args(i + 1)
        else if (args(i).equalsIgnoreCase("--port")) port = (i + 1).toInt
      }
      app.startup(editorFactory, userid, hostip, port, false, isRoboClient, waitTime, workspace)
    } catch {
      case ex: RuntimeException => org.nlogo.api.Exceptions.handle(ex)
    }
  }
}

class ClientApp extends JFrame("HubNet") with ErrorHandler with ClientAppInterface {
  import ClientApp.localClientIndex

  private var clientPanel: ClientPanel = _
  private var loginDialog: LoginDialog = _
  private var isLocal: Boolean = _

  locally {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    setResizable(false)
  }

  def startup(editorFactory: EditorFactory, userid: String, hostip: String,
              port: Int, isLocal: Boolean, isRobo: Boolean, waitTime: Long, workspace: CompilerServices) {
    EventQueue.invokeLater(() => {
      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        def uncaughtException(t: Thread, e: Throwable) {
          org.nlogo.api.Exceptions.handle(e)
        }
      })

      this.isLocal = isLocal
      setIconImage(Images.loadImageResource("/images/arrowhead.gif"))
      getContentPane.setLayout(new BorderLayout())
      loginDialog = new LoginDialog(this, userid, hostip, port, false)
      clientPanel =
        if (isRobo) new RoboClientPanel(editorFactory, this, waitTime, workspace)
        else new ClientPanel(editorFactory, this, workspace)

      getContentPane.add(clientPanel, BorderLayout.CENTER)
      pack()
      Positioning.center(this, null)

      if (isLocal) {
        val killLocalListener = () => {
          clientPanel.logout()
          ClientApp.this.dispose()
        }

        addWindowListener(killLocalListener)
        loginDialog.addWindowListener(killLocalListener)

        // increment first, otherwise if two local clients are
        // started in rapid succession they might collide
        // ev 7/30/08
        localClientIndex += 1
        login("Local " + localClientIndex, hostip, port)
      }
      else {
        addWindowListener(() => handleExit())
        Positioning.center(loginDialog, null)
        loginDialog.addWindowListener(() => handleQuit())
        doLogin()
      }
    })
  }

  private def doLogin() {
    /// arggh.  isn't there some way around keeping this flag??
    /// grumble. ev 7/29/08
    if (!isLocal){
      loginDialog.go(new LoginCallback {
        def apply(user: String, host: String, port: Int) { login(user, host, port) }
      })
    }
  }

  def completeLogin() { setVisible(true) }

  private def login(userid: String, hostip: String, port: Int) {
    var exs: Option[String] = None
    ModalProgressTask.onUIThread(Hierarchy.getFrame(this), "Entering...", () => {
      exs = clientPanel.login(userid, hostip, port)
    })
    exs match {
      case Some(ex) =>
        handleLoginFailure(ex)
        clientPanel.disconnect(ex.toString)
      case None =>
        loginDialog.setVisible(false)
        clientPanel.requestFocus()
    }
  }

  def showExitMessage(title: String, message: String): Boolean = {
    EventQueue.mustBeEventDispatchThread()
    val buttons = Array[Object](title, I18N.gui.get("common.buttons.cancel"))
    0 == OptionDialog.show(loginDialog, "Confirm " + title, message, buttons)
  }

  def handleDisconnect(activityName: String, connected: Boolean, reason:String) {
    EventQueue.mustBeEventDispatchThread()
    if (isLocal) this.dispose()
    else {
      if (connected) {
        OptionDialog.show(this, "", "You have been disconnected from " + activityName + ".", Array("ok"))
        dispose()
        doLogin()
        ()
      }
    }
  }

  def handleLoginFailure(errorMessage: String) {
    EventQueue.mustBeEventDispatchThread()
    OptionDialog.show(ClientApp.this, "Login Failed",
      errorMessage, Array(I18N.gui.get("common.buttons.ok")))
    loginDialog.setVisible(true)
  }

  def handleExit() {
    EventQueue.mustBeEventDispatchThread()
    if (showExitMessage(I18N.gui.get("common.buttons.exit"), "Do you really want to exit this activity?")){
      clientPanel.logout()
      setVisible(false)
      dispose()
      doLogin()
    }
  }

  def handleQuit() {
    EventQueue.mustBeEventDispatchThread()
    val shouldExit = showExitMessage(
      I18N.gui.get("common.buttons.quit"),
      "Do you really want to quit HubNet?")
    if (shouldExit) System.exit(0)
  }
}
