// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tools

import org.nlogo.api.{ FileIO, NetLogoLegacyDialect }
import org.nlogo.app.infotab.InfoFormatter
import java.io.{ FileInputStream, FileOutputStream }

/**
 * inputs: models/Code Examples/Info Tab Example.nlogo
 * outputs: docs/infotab.html
 * really.
 */
object InfoTabDocGenerator {
  def main(args:Array[String]) {
    val model = FileIO.fileToString("./models/Code Examples/Info Tab Example.nlogo")
    val info = model.split("\\@\\#\\$\\#\\@\\#\\$\\#\\@(\r)?\n")(2)
    val pre = Preprocessor.convert(info)
    val html = InfoFormatter.toInnerHtml(pre, NetLogoLegacyDialect)
    val post = Postprocessor.convert(html)
    //println(start + post)
    println("writing ./docs/infotab.html")
    FileIO.writeFile("./docs/infotab.html", start + post + "\n")

    def copy(from: String, to: String) {
      val (in, out) = (new FileInputStream(from), new FileOutputStream(to))
      val buffer = new Array[Byte](1024)
      Iterator.continually(in.read(buffer)).takeWhile(_ != -1).foreach { out.write(buffer, 0, _) }
    }

    println("cp ./models/Code Examples/Perspective Example.png ./docs/Perspective Example.png")
    copy("./models/Code Examples/Perspective Example.png", "./docs/Perspective Example.png")
  }

  // runs on the wiki text, before it gets converted to HTML
  object Preprocessor {
    lazy val convert: String => String = List(
      dropWhatIsItSection,
      createTOC,
      insertAIntoHeaders).reduceLeft(_ andThen _)

    private val dropWhatIsItSection = (s:String) => {
      s.split("\n").drop(1). // drop the ## WHAT IS IT? line
        dropWhile( line => ! line.startsWith("##") ). // drop the rest of the section
        mkString("\n")
    }

    // find header lines (## ) and wrap them with html <a> blocks
    // so that they can be linked to from the TOC
    private val insertAIntoHeaders = (s: String) => {
      (for(line <- s.split("\n")) yield
        if (line.startsWith("## ")) {
          val header = line.substring(3)
          val name = header.replace(' ', '_').toLowerCase
          "<h2><a name=\"" + name + "\"" + " id=\"" + name + "\">" + header + "</a></h2>"
        } else line).mkString("\n")
    }

    // create links to all the header sections.
    private val createTOC = (s:String) => {
       val toc = "<ul>" + {
         s.split("\n").filter(_.startsWith("## ")).map{ line =>
           val header = line.substring(3)
           val name = header.replace(' ', '_').toLowerCase
           "<li><a href=\"#" + name + "\">" + header + "</a></li>"
         }
       }.mkString("\n") + "</ul>"
      toc + "\n\n" + s
    }
  }

  // runs on the HTML, after the wiki text gets converted to HTML
  object Postprocessor {
    lazy val convert: String => String =
      List(h4ToH4, ldquoToquot, rdquoToquot, mdashTo_--, lsquoToTick, rsquoToTick).reduceLeft(_ andThen _)
    // h4's don't look quite right with the web pages css. convert to h5
    private val h4ToH4 = (s: String) => s.replace("h4>", "h5>")
    private val ldquoToquot = (s: String) => s.replace("&ldquo;", "&quot;")
    private val rdquoToquot = (s: String) => s.replace("&rdquo;", "&quot;")
    private val mdashTo_-- = (s: String) => s.replace("&mdash;", "--")
    private val lsquoToTick = (s: String) => s.replace("&lsquo;", "'")
    private val rsquoToTick = (s: String) => s.replace("&rsquo;", "'")
  }

  // stolen from interface.html, its stuff that goes at the start of infotab.html
  val start = """<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
"http://www.w3.org/TR/html4/strict.dtd">
<title>
      NetLogo User Manual: Info Tab
    </title>
    <link rel="stylesheet" href="netlogo.css" type="text/css">
    <meta http-equiv="Content-Type" content="text/html; charset=us-ascii">

<h1>
  <a name="information" id="information">Info Tab</a>
</h1>
<p>
  The Info tab provides an introduction to a model.
  It explains what system is being modeled, how the model was
  created, and and how to use it.
  It may also suggest things to explore and ways to extend the model, or
  call your attention to particular NetLogo features the model uses.
<p class="screenshot">
  <img alt="screen shot" src="images/interface/infotab.png">
<p>
  You may wish to read the Info tab before starting a model.
<h2>
  <a name="info-editing" id="information">Editing</a>
</h2>
<p>
  The normal, formatted view of the Info tab is not editable. To make edits, click
  the &quot;Edit&quot; button. When done editing, click the
  &quot;Edit&quot; button again.
<p class="screenshot">
  <img alt="screen shot" src="images/interface/infotabedit.png">
<p>
  You edit the Info tab as unformatted plain text. When you're done
  editing, the plain text you entered is displayed in a more attractive
  format.
<p>
  To control how the formatted display looks, you use a &quot;markup
  language&quot; called Markdown. You may have encountered Markdown
  elsewhere; it is used on a number of web sites. (There are other
  markup languages in use on the web; for example, Wikipedia used a
  markup language called MediaWiki. Markup languages differ
  in details.)
<p>
  The remainder of this guide is a tour of Markdown.


"""
}
