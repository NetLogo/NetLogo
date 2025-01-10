// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import java.awt.BorderLayout
import javax.swing.{WindowConstants, JFrame}
import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.window.{ ClientAppInterface, DefaultEditorFactory }
import org.nlogo.swing.{ Implicits, ModalProgressTask, OptionPane, SetSystemLookAndFeel }, Implicits._
import org.nlogo.theme.InterfaceColors
import org.nlogo.awt.{ Hierarchy, Images, Positioning, EventQueue }
import org.nlogo.hubnet.connection.Ports

/**
 * The HubNet client.
 */
object ClientApp {
  private var localClientIndex = 0

  // called by App.main()
  def mainHelper(args: Array[String], workspace: CompilerServices) {
    try {
      val app = new ClientApp()
      SetSystemLookAndFeel.setSystemLookAndFeel()

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
      app.startup(userid, hostip, port, false, isRoboClient, waitTime, workspace)
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

  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
  setResizable(false)

  def startup(userid: String, hostip: String,
              port: Int, isLocal: Boolean, isRobo: Boolean, waitTime: Long, compiler: CompilerServices): Unit = {
    val editorFactory = new DefaultEditorFactory(compiler)
    EventQueue.invokeLater(() => {
      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        def uncaughtException(t: Thread, e: Throwable) {
          org.nlogo.api.Exceptions.handle(e)
        }
      })

      this.isLocal = isLocal
      setIconImage(Images.loadImageResource("/images/hubnet.png"))
      getContentPane.setLayout(new BorderLayout())
      loginDialog = new LoginDialog(this, userid, hostip, port)
      clientPanel =
        if (isRobo) new RoboClientPanel(editorFactory, this, waitTime, compiler)
        else new ClientPanel(editorFactory, this, compiler)

      getContentPane.add(clientPanel, BorderLayout.CENTER)

      pack()

      Positioning.center(this, null)

      syncTheme()

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

  def completeLogin() {
    setVisible(true)
  }

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
    new OptionPane(loginDialog, title, message, OptionPane.Options.OK_CANCEL,
                   OptionPane.Icons.INFO).getSelectedIndex == 0
  }

  def handleDisconnect(activityName: String, connected: Boolean, reason:String) {
    EventQueue.mustBeEventDispatchThread()
    if (isLocal) this.dispose()
    else if (connected) {
      new OptionPane(this, I18N.gui.get("edit.hubnet.disconnected"),
                     I18N.gui.getN("edit.hubnet.disconnected.message", activityName), OptionPane.Options.OK,
                     OptionPane.Icons.INFO)
      dispose()
      doLogin()
      ()
    }
  }

  def handleLoginFailure(errorMessage: String) {
    EventQueue.mustBeEventDispatchThread()
    new OptionPane(ClientApp.this, I18N.gui.get("edit.hubnet.loginFailed"), errorMessage, OptionPane.Options.OK,
                   OptionPane.Icons.ERROR)
    loginDialog.setVisible(true)
  }

  def handleExit() {
    EventQueue.mustBeEventDispatchThread()
    if (showExitMessage(I18N.gui.get("edit.hubnet.exit"), I18N.gui.get("edit.hubnet.exit.message"))) {
      clientPanel.logout()
      setVisible(false)
      dispose()
      doLogin()
    }
  }

  def handleQuit() {
    EventQueue.mustBeEventDispatchThread()
    val shouldExit = showExitMessage(I18N.gui.get("edit.hubnet.quit"), I18N.gui.get("edit.hubnet.quit.message"))
    if (shouldExit) System.exit(0)
  }

  def syncTheme() {
    getContentPane.setBackground(InterfaceColors.DIALOG_BACKGROUND)

    clientPanel.syncTheme()
  }
}
