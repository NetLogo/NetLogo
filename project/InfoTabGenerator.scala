import sbt._

import org.pegdown.{ PegDownProcessor, Extensions => PegdownExtensions }

object InfoTabGenerator {
  def apply(model: File): String = {
    val modelText = IO.read(model)
    val info = modelText.split("\\@\\#\\$\\#\\@\\#\\$\\#\\@(\r)?\n")(2)
    val pre = Preprocessor.convert(info)
    val html = pegdown(pre)
    val r = Postprocessor.convert(html)
    r
  }

  def pegdown(str: String): String = {
    new PegDownProcessor(PegdownExtensions.SMARTYPANTS |       // beautifies quotes, dashes, etc.
                         PegdownExtensions.AUTOLINKS |         // angle brackets around URLs and email addresses not needed
                         PegdownExtensions.HARDWRAPS |         // GitHub flavored newlines
                         PegdownExtensions.FENCED_CODE_BLOCKS) // delimit code blocks with ```
      .markdownToHtml(str)
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
    private val lsquoToTick = (s: String) => s.replace("&lsquo;", "&apos;")
    private val rsquoToTick = (s: String) => s.replace("&rsquo;", "&apos;")
  }
}

