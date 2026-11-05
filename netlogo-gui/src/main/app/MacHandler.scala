// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Desktop
import java.awt.desktop.{ AboutHandler, AboutEvent, OpenFilesEvent, OpenFilesHandler, PreferencesEvent,
                          PreferencesHandler, QuitEvent, QuitHandler, QuitResponse }

import org.nlogo.util.{ AppHandler, Application }

class MacHandler extends AppHandler {
  private var app: Option[Application] = None
  private var openLater: String = ""

  locally {
    val application: Desktop = Desktop.getDesktop

    application.setOpenFileHandler(new OpenFilesHandler {
      override def openFiles(e: OpenFilesEvent): Unit = {
        doOpen(e.getFiles.get(0).getAbsolutePath)
      }
    })

    application.setPreferencesHandler(new PreferencesHandler {
      override def handlePreferences(e: PreferencesEvent): Unit = {
        app.foreach(_.handleShowPreferences())
      }
    })

    application.setAboutHandler(new AboutHandler {
      override def handleAbout(e: AboutEvent): Unit = {
        app.foreach(_.handleShowAbout())
      }
    })

    application.setQuitHandler(new QuitHandler {
      override def handleQuitRequestWith(e: QuitEvent, response: QuitResponse): Unit = {
        app.foreach(_.handleQuit())
      }
    })
  }

  override def ready(app: Application): Unit = {
    this.app = Option(app)

    if (openLater.trim.nonEmpty)
      doOpen(openLater)
  }

  private def doOpen(path: String): Unit = {
    app match {
      case Some(instance) =>
        instance.handleOpenPath(path)

      case _ =>
        openLater = path
    }
  }
}
