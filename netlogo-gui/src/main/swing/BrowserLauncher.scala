// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Desktop }
import java.io.IOException
import java.lang.Process
import java.net.{ NetworkInterface, URI, URISyntaxException }
import java.nio.file.{ Files, Path, Paths }
import javax.swing.JDialog

import org.nlogo.core.I18N

import scala.jdk.CollectionConverters.EnumerationHasAsScala

object BrowserLauncher {
  private val osName = System.getProperty("os.name")
  val unableToOpenBrowserError = "We were unable to open a browser on your system.\n" +
      "This error can be reported to bugs@ccl.northwestern.edu"

  def openURI(comp: Component, uri: URI): Unit = {
    val normalUri = uri.normalize()
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

  def tryOpenURI(comp: Component, uri: URI, fallback: Path, anchor: String = ""): Unit = {
    if (NetworkInterface.getNetworkInterfaces.asScala.exists(_.isUp)) {
      openURI(comp, uri)
    } else {
      openPath(comp, fallback, anchor)
    }
  }

  def openPath(comp: Component, path: Path, anchor: String): Unit = {
    val u = path.toUri
    if (anchor == null || anchor == "")
      openURI(comp, u)
    else {
      // Windows and Linux don't support anchors in file URLs, so we create a redirect helper
      // to work around this.
      val uriWithAnchor = new URI(u.getScheme, u.getHost, u.getPath, anchor)
      val redirectFile = Files.createTempFile("redirectHelper", ".html")
      Files.write(redirectFile, redirectHelper(uriWithAnchor).getBytes("UTF-8"))
      openURI(comp, redirectFile.toUri)
    }
  }

  def openPath(path: Path, anchor: String): Unit = {
    openPath(new JDialog, path, anchor)
  }

  private def redirectHelper(targetUri: URI): String = {
    s"""|<html>
        |<head><meta http-equiv="refresh" content="0;url=${targetUri}" /></head>
        |<body>
        |<script type="text/javascript">onload="window.location = '${targetUri}'"</script>
        |</body>
        |</html>""".stripMargin
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

  def docPath(docName: String): Path = {
    val docRoot = System.getProperty("netlogo.docs.dir", "docs")
    Paths.get(docRoot + "/" + docName)
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
