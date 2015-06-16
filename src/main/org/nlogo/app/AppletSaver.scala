// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

// The need to do buf.clear() here is peculiar -- this should be refactored to be
// more immutable. - ST 12/8/10

// should use Scala collections once we get FileMenu converted to Scala - ST 8/6/10
import java.util.{List => JList}
import collection.JavaConversions._
import org.nlogo.api.{I18N, FileIO, Version}

class AppletSaver(proceduresToHtml: ProceduresToHtmlInterface, buf: StringBuilder) {

  // could just be a default argument once FileMenu is in Scala - ST 8/6/10
  def this(proceduresToHtml: ProceduresToHtmlInterface) =
    this(proceduresToHtml, new StringBuilder)

  val url = "http://ccl.northwestern.edu/netlogo/"
  val siteLink = "<a target=\"_blank\" href=\"" + url + "\">NetLogo</a>"
  val eol = System.getProperty("line.separator")

  def save(frame: java.awt.Frame, iP: InterfacePanel, modelName: String,
           path: String, fileName: String, infoText: String, procedures: String,
           additionalJars: JList[String], extensions: JList[String]) = {
    // we add a 10 pixel buffer here so that widgets are not cramped and in some cases cut off
    // slightly on some platforms, such as Firefox on Ubuntu Linux. -- CLB
    // + 16 to width so there is space for the NetLogo ad ev 7/9/08
    var width = iP.getPreferredSize(true).width + 10 + 16  // true = ignore command center
    // this is pretty crappy. The applet and the application have different minimum widths for the
    // view widget. (because there is a speed slider in the applet control strip) we want to make sure
    // the width the applet saves is wide enough for the applet with the slightly wider view.  The minimum
    // width is 230.  However, because the applet view isn't in an interface panel we don't strictly know that
    // the extra space is necessary since the view might not be at the edge of the interface but setting it
    // up so we can actually measure that is hairy and seems like overkill. ev 11/21/08
    val view = iP.viewWidget.asWidget
    val viewWidth = view.getWidth
    if(viewWidth < 280)
      width += 280 - viewWidth
    var height = iP.getPreferredSize(true).height + 10
    val viewHeight = view.getHeight()
    if(viewHeight < 280)
      height += 280 - viewHeight
    build(modelName, fileName, width, height, infoText, procedures, additionalJars, extensions)
    doSave(frame, path)
  }

  def saveClient(frame: java.awt.Frame, width: Int, height: Int, modelName: String, path: String) = {
    // + 16 to width so there is space for the NetLogo ad ev 11/10/08
    // 88 == the size of the message area ev 8/6/8
    buildClient(modelName, width + 10 + 16, height + 10 + 88)
    doSave(frame, path)
  }

  private def doSave(frame: java.awt.Frame, path: String) {
    org.nlogo.swing.ModalProgressTask(
      frame, "Saving...",
      new Runnable() { def run() {
        try {
          FileIO.writeFile(path, buf.toString)
          org.nlogo.swing.OptionDialog.show(
            frame, "Applet Saved",
            "The HTML file you have saved contains instructions for " +
            "arranging the necessary files on your web server.  "  +
            "You may view or edit the HTML file in any text editor.",
            Array(I18N.gui.get("common.buttons.ok")))
        }
        catch {
          case ex: java.io.IOException =>
            javax.swing.JOptionPane.showMessageDialog(
              frame, ex.getMessage, I18N.gui.get("common.messages.error"),
              javax.swing.JOptionPane.ERROR_MESSAGE)
        } } } )
  }

  def buildClient(modelName: String, width: Int, height: Int) {
    buf.clear()
    header(modelName)
    paragraph("In order for this to work, this file and the file HubNet.jar",
              "must be in the same directory.  (You can copy HubNet.jar",
              "from the directory where you installed NetLogo.)")
    paragraph("On some systems, you can test the applet locally on your computer",
              "before uploading it to a web server.  It doesn't work on all systems,",
              "though, so if it doesn't work from your hard drive, please try",
              "uploading it to a web server.")
    paragraph("You don't need to include everything in this file in your page.",
              "If you want, you can just take the HTML code beginning with",
              "&lt;applet&gt; and ending with &lt;/applet&gt;, and paste it into any HTML",
              "file you want.  It's even OK to put multiple &lt;applet&gt; tags",
              "on a single page.")
    paragraph("If HubNet.jar is in a different directory you must",
              "modify the archive= line in the HTML code to point",
              "to its actual location. (For example, if you have ",
              "multiple applets in different directories on the same",
              "web server, you may want to put a single copy of",
              "HubNet.jar in one central place and change the",
              "archive= lines of all the HTML files to point to that",
              "one central copy.  This will save disk space for you and",
              "download time for your users.)")
    paragraph("To run this activity you must run the server in NetLogo",
              "on the same server that you are hosting the webpage. This",
              "webpage must be accessible to the webserver user.  To login",
              "users should navigate to this webpage and enter a user name",
              "and the port as displayed in the HubNet Control Center.")
    applet("org.nlogo.hubnet.client.ClientApplet", "HubNet.jar", width, height)
    line("<p>powered by " + siteLink + "</p>")
    line()
    line("</body>")
    line("</html>")
  }

  // non-private for unit testing - ST 2/8/05

  def build(modelName: String, fileName: String, width: Int, height: Int,
            infoText: String, procedures: String, additionalJars: JList[String], extensions: JList[String]) = {
    buf.clear()
    header(modelName)
    paragraph("In order for this to work, this file, your model file",
              "(" + fileName + "), and the files NetLogoLite.jar and NetLogoLite.jar.pack.gz",
              "must all be in the same directory.  (You can copy NetLogoLite.jar and NetLogoLite.jar.pack.gz",
              "from the directory where you installed NetLogo.)")
    if(extensions.nonEmpty) {
      val str = new StringBuilder()
      val size = extensions.size
      var i = 0
      for(jar <- extensions) {
        i += 1
        if(i == size && size > 1)
          str ++= "and "
        str ++= jar + " "
      }
      str ++= (if (size > 1) "extensions." else "extension.")
      paragraph("The applet will also need access to the ", str.toString,
                "Copy the entire directory for each required extension into the same",
                "directory as the applet.")
    }
    paragraph("On some systems, you can test the applet locally on your computer",
              "before uploading it to a web server.  It doesn't work on all systems,",
              "though, so if it doesn't work from your hard drive, please try",
              "uploading it to a web server.")
    paragraph("You don't need to include everything in this file in your page.",
              "If you want, you can just take the HTML code beginning with",
              "&lt;applet&gt; and ending with &lt;/applet&gt;, and paste it into any HTML",
              "file you want.  It's even OK to put multiple &lt;applet&gt; tags",
              "on a single page.")
    paragraph("If the NetLogoLite files and your model are in different",
              "directories, you must modify the archive= and value= lines",
              "in the HTML code to point to their actual locations.",
              "(For example, if you have multiple applets in different",
              "directories on the same web server, you may want to put",
              "a single copy of the NetLogoLite files in one central place and",
              "change the archive= lines of all the HTML files to point",
              "to that one central copy.  This will save disk space for",
              "you and download time for your users.)")
    applet("org.nlogo.lite.Applet", "NetLogoLite.jar", width, height,
           List(("DefaultModel", org.nlogo.util.Utils.escapeSpacesInURL(fileName)),
                ("java_arguments", "-Djnlp.packEnabled=true")),
           additionalJars)
    line("<p>powered by")
    line(siteLink + "</p>")
    line()
    line("<p>view/download model file:")
    line("<a href=\"" + org.nlogo.util.Utils.escapeSpacesInURL(fileName) +
         "\">" + fileName + "</a></p>")
    line(InfoFormatter.toInnerHtml(infoText) + "\n")
    line(InfoFormatter.toInnerHtml("# CODE"))
    line(proceduresToHtml.convert(procedures))
    line("</body>")
    line("</html>")
  }

  private def header(modelName: String) {
    line("<html>")
    line("<head>")
    line("<title>" + modelName + "</title>")
    for(l <- InfoFormatter.styleSheet(12).split("\n"))
      line(l)
    line("</head>")
    line("<body>")
    line()
    line("<p><b>NOTICE:</b> The NetLogo team no longer recommends using NetLogo applets. <a href=\"https://github.com/NetLogo/NetLogo/wiki/Applets\">Details here</a>.</p>")
    line()
    line("<p>This page was automatically generated by " + Version.version + ".</p>")
    line()
    // note that this next paragraph is duplicated in Applet - ST 5/10/05
    line("<p>The applet requires Java 5 or higher.")
    line("Java must be enabled in your browser settings.")
    line("Mac users must have Mac OS X 10.4 or higher.")
    line("Windows and Linux users may obtain the latest Java from")
    line("<a href=\"http://www.java.com/\">Oracle's Java site</a>.</p>")
    line()
    line("<p><hr>")
    line()
  }

  private def applet(code: String, archive: String,
                     width: Int, height: Int,
                     params: List[(String,String)] = Nil,
                     additionalJars: JList[String] = Nil) = {
    line("<p>")
    line("<applet code=\"" + code + "\"")
    line("        archive=" + (archive :: additionalJars.toList).mkString("\"", ",", "\""))
    line("        width=\"" + width + "\" height=\"" + height + "\">")
    for(param <- params) {
      line("  <param name=\"" + param._1 + "\"")
      line("         value=\"" + param._2 + "\">")
    }
    line("</applet>")
    line("</p>")
    line()
  }

  private def paragraph(lines: String*) {
    line(lines.mkString("<p><font size=\"-1\">", eol, "</font></p>"))
    line()
  }

  private def line(line: String = "") {
    buf.append(line + eol)
  }

}
