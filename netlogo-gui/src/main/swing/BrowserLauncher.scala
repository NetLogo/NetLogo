// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Desktop }
import java.io.IOException
import java.lang.Process
import java.net.{ HttpURLConnection, NetworkInterface, URI, URISyntaxException, URL }
import java.nio.file.{ Files, Path, Paths, StandardOpenOption }
import javax.swing.JDialog

import org.nlogo.core.I18N

import scala.jdk.CollectionConverters.EnumerationHasAsScala
import scala.util.Try

object BrowserLauncher {
  private val osName = System.getProperty("os.name")
  val unableToOpenBrowserError = "We were unable to open a browser on your system.\n" +
      "This error can be reported to bugs@ccl.northwestern.edu"

  // used by GUI tests, prevents the inevitable onslaught of random tabs being opened (Isaac B 11/5/25)
  private var automated = false

  def setAutomated(automated: Boolean): Unit = {
    this.automated = automated
  }

  def openURI(comp: Component, uri: URI): Unit = {
    val normalUri = uri.normalize()

    if (automated)
      return

    try {
      val opened =
        desktop.map( d => { d.browse(normalUri); true } ).getOrElse(false)
      if (!opened)
        osSpecificBrowserRunner.getOrElse(browserNotFound()).apply(normalUri.toString)
    } catch {
      case ex: UnsupportedOperationException =>
        new OptionPane(comp, I18N.gui.get("common.messages.error"), unableToOpenBrowserError, OptionPane.Options.Ok,
                       OptionPane.Icons.Error)
      case ex: BrowserNotFoundException =>
        new OptionPane(comp, I18N.gui.get("common.messages.error"), ex.getLocalizedMessage, OptionPane.Options.Ok,
                       OptionPane.Icons.Error)
      case ex: IOException =>
        new OptionPane(comp, I18N.gui.get("common.messages.error"),
                       s"""Unable to open a browser to: ${normalUri.toString}
                       ${if (normalUri.toString != uri.toString) { s"with original URI: ${uri.toString}" } else { "" }}
                       Please report to bugs@ccl.northwestern.edu""", OptionPane.Options.Ok, OptionPane.Icons.Error)
    }
  }

  def openURI(uri: URI): Unit = {
    openURI(new JDialog, uri)
  }

  def openURIString(s: String): Unit = {
    val dialog = new JDialog
    val uri = makeURI(dialog, s)
    openURI(dialog, uri)
  }

  def tryOpenURI(comp: Component, uri: URI, fallback: Path): Unit = {
    if (hasConnection()) {
      openURI(comp, uri)
    } else {
      openPath(comp, fallback)
    }
  }

  // non-browser PDF viewers typically ignore instructions to go to a particular place in the PDF,
  // so this little hack ensures that it gets opened in a browser. (Isaac B 2/1/26)
  def openPath(comp: Component, path: Path): Unit = {
    val temp = Paths.get(System.getProperty("user.home"), ".nlogo", "load-pdf.html")

    temp.toFile.deleteOnExit()

    val html = s"<html><script>window.location = \"file://$path\";</script></html>"

    Files.writeString(temp, html, StandardOpenOption.CREATE)

    openURI(comp, temp.toUri)
  }

  def makeURI(comp: Component, s: String): URI = {
    try {
      new URI(s)
    } catch {
      case ue: URISyntaxException =>
        new OptionPane(comp, I18N.gui.get("common.messages.error"),
                       s"Unable to open a browser to: $s\n" + "Please report to bugs@ccl.northwestern.edu",
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
      null
    }
  }

  // first check if there's a valid network interface, then ensure that it can open a connection to the docs
  // as a connection test (Isaac B 9/26/25)
  private def hasConnection(): Boolean = {
    NetworkInterface.getNetworkInterfaces.asScala.exists(_.isUp) &&
      Try(new URL("https://docs.netlogo.org").openConnection.asInstanceOf[HttpURLConnection].getResponseCode)
        .toOption.contains(200)
  }

  private def desktop: Option[Desktop] = {
    if (Desktop.isDesktopSupported) Some(Desktop.getDesktop)
    else None
  }

  @throws(classOf[BrowserNotFoundException])
  private def osSpecificBrowserRunner: Option[String => Unit] = {
    if (osName.startsWith("Windows"))
      Some((urlString: String) =>
          runCommand(Array[String]("cmd.exe", "/c", "start", "\"\"", s""""$urlString"""")))
    else if (osName.startsWith("Mac"))
      None
    else
      Some( {(urlString: String) =>
        try {
          val exitCode =
            runCommand(Array[String]("firefox", "-remote", s"'openURL($urlString)'")).waitFor()
          if (exitCode != 0) {  // if Firefox was not open
            runCommand(Array[String]("firefox", urlString))
          }
        } catch {
          case ie: InterruptedException => throw new IllegalStateException(ie)
          case ex: IOException => firefoxNotFound(ex)
        }
      })
  }

  private def runCommand(cmd: Array[String]): Process = {
    Runtime.getRuntime().exec(cmd)
  }

  private def browserNotFound(): Nothing = {
    throw new BrowserNotFoundException(
      unableToOpenBrowserError);
  }

  private def firefoxNotFound(ex: Exception): Nothing = {
    throw new BrowserNotFoundException(
      "NetLogo could not find and execute a web browser named \'firefox\'." +
      "Please install Firefox and ensure that the \'firefox\' command " +
      "is in your executable PATH.  Firefox is available here:\n " +
      "http://www.mozilla.com/firefox/\n\n" +
      "The full error message was:\n " + ex.getLocalizedMessage()
    )
  }

  class BrowserNotFoundException(message: String) extends Exception(message)
}
